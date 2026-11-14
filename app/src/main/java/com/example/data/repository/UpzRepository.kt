package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.AmilProfile
import com.example.data.model.MustahikRecord
import com.example.data.model.StatusPenyaluran
import com.example.data.model.StatusTransaksi
import com.example.data.model.WilayahCikulakKidul
import com.example.data.model.ZakatTransaction
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UpzRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth,
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Petugas harus login dengan Google terlebih dahulu.")
    }

    private fun extractFirestoreException(error: Throwable): FirebaseFirestoreException? {
        return generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
    }

    fun observeProfile(userId: String): Flow<AmilProfile?> = flow {
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            AmilProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val fsError = extractFirestoreException(error)
                    if (fsError == null && error is CancellationException) throw error
                    val toLog = fsError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureDefaultProfile(displayName: String?): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val cleanName = displayName?.trim()?.takeIf { it.length >= 2 }?.take(80)
                    ?: "Petugas Amil UPZ"
                val payload = mapOf(
                    "userId" to uid,
                    "namaPetugas" to cleanName,
                    "jabatan" to "Petugas Amil Dusun",
                    "dusun" to WilayahCikulakKidul.daftarDusun.first(),
                    "desa" to WilayahCikulakKidul.NAMA_DESA_LENGKAP,
                    "targetFitrahBerasKg" to 2500.0,
                    "targetZakatUang" to 50_000_000L,
                    "hargaBerasPerKg" to 14_000L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                docRef.set(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateProfile(
        namaPetugas: String,
        jabatan: String,
        dusun: String,
        targetFitrahBerasKg: Double,
        targetZakatUang: Long,
        hargaBerasPerKg: Long,
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val updates = mapOf(
            "namaPetugas" to namaPetugas.trim().take(80),
            "jabatan" to jabatan.trim().take(60),
            "dusun" to dusun.trim().take(60),
            "desa" to WilayahCikulakKidul.NAMA_DESA_LENGKAP,
            "targetFitrahBerasKg" to targetFitrahBerasKg.coerceAtLeast(0.0),
            "targetZakatUang" to targetZakatUang.coerceAtLeast(0L),
            "hargaBerasPerKg" to hargaBerasPerKg.coerceIn(1000L, 500_000L),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeTransactions(userId: String): Flow<List<ZakatTransaction>> = flow {
        val colRef = db.collection("users").document(userId).collection("zakat_transactions")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ZakatTransaction::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val fsError = extractFirestoreException(error)
                    if (fsError == null && error is CancellationException) throw error
                    val toLog = fsError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun addTransaction(
        namaMuzakki: String,
        rtRw: String,
        jenisZakat: String,
        jumlahJiwa: Long,
        nominalUang: Long,
        beratBerasKg: Double,
        metodePembayaran: String,
        catatan: String,
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("zakat_transactions")
        val docRef = colRef.document()
        val dateStamp = SimpleDateFormat("yyMMdd", Locale.US).format(Date())
        val suffix = docRef.id.take(4).uppercase(Locale.US)
        val nomorKwitansi = "UPZ-CK-$dateStamp-$suffix"

        val payload = mapOf(
            "userId" to uid,
            "nomorKwitansi" to nomorKwitansi,
            "namaMuzakki" to namaMuzakki.trim().take(100),
            "rtRw" to rtRw.trim().take(60),
            "jenisZakat" to jenisZakat,
            "jumlahJiwa" to jumlahJiwa.coerceIn(0L, 100L),
            "nominalUang" to nominalUang.coerceAtLeast(0L),
            "beratBerasKg" to beratBerasKg.coerceAtLeast(0.0),
            "metodePembayaran" to metodePembayaran,
            "catatan" to catatan.trim().take(250),
            "status" to StatusTransaksi.TERCATAT.key,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateTransaction(tx: ZakatTransaction): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(tx.id.isNotBlank()) { "ID transaksi wajib diisi" }
        val docRef = db.collection("users").document(uid)
            .collection("zakat_transactions").document(tx.id)
        val updates = mapOf(
            "nomorKwitansi" to tx.nomorKwitansi.trim().take(40),
            "namaMuzakki" to tx.namaMuzakki.trim().take(100),
            "rtRw" to tx.rtRw.trim().take(60),
            "jenisZakat" to tx.jenisZakat,
            "jumlahJiwa" to tx.jumlahJiwa.coerceIn(0L, 100L),
            "nominalUang" to tx.nominalUang.coerceAtLeast(0L),
            "beratBerasKg" to tx.beratBerasKg.coerceAtLeast(0.0),
            "metodePembayaran" to tx.metodePembayaran,
            "catatan" to tx.catatan.trim().take(250),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun verifyTransaction(txId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(txId.isNotBlank()) { "ID transaksi wajib diisi" }
        val docRef = db.collection("users").document(uid)
            .collection("zakat_transactions").document(txId)
        val updates = mapOf(
            "status" to StatusTransaksi.TERVERIFIKASI.key,
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteTransaction(txId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(txId.isNotBlank()) { "ID transaksi wajib diisi" }
        val docRef = db.collection("users").document(uid)
            .collection("zakat_transactions").document(txId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeMustahik(userId: String): Flow<List<MustahikRecord>> = flow {
        val colRef = db.collection("users").document(userId).collection("mustahik")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MustahikRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val fsError = extractFirestoreException(error)
                    if (fsError == null && error is CancellationException) throw error
                    val toLog = fsError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun addMustahik(
        namaMustahik: String,
        golonganAsnaf: String,
        rtRw: String,
        bantuanUang: Long,
        bantuanBerasKg: Double,
        catatan: String,
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("mustahik")
        val docRef = colRef.document()
        val payload = mapOf(
            "userId" to uid,
            "namaMustahik" to namaMustahik.trim().take(100),
            "golonganAsnaf" to golonganAsnaf,
            "rtRw" to rtRw.trim().take(60),
            "statusPenyaluran" to StatusPenyaluran.TERDAFTAR.key,
            "bantuanUang" to bantuanUang.coerceAtLeast(0L),
            "bantuanBerasKg" to bantuanBerasKg.coerceAtLeast(0.0),
            "catatan" to catatan.trim().take(250),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateMustahik(record: MustahikRecord): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(record.id.isNotBlank()) { "ID mustahik wajib diisi" }
        val docRef = db.collection("users").document(uid)
            .collection("mustahik").document(record.id)
        val updates = mapOf(
            "namaMustahik" to record.namaMustahik.trim().take(100),
            "golonganAsnaf" to record.golonganAsnaf,
            "rtRw" to record.rtRw.trim().take(60),
            "bantuanUang" to record.bantuanUang.coerceAtLeast(0L),
            "bantuanBerasKg" to record.bantuanBerasKg.coerceAtLeast(0.0),
            "catatan" to record.catatan.trim().take(250),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun distributeMustahik(
        mustahikId: String,
        bantuanUang: Long,
        bantuanBerasKg: Double,
        catatan: String,
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(mustahikId.isNotBlank()) { "ID mustahik wajib diisi" }
        val docRef = db.collection("users").document(uid)
            .collection("mustahik").document(mustahikId)
        val updates = mapOf(
            "statusPenyaluran" to StatusPenyaluran.DISALURKAN.key,
            "bantuanUang" to bantuanUang.coerceAtLeast(0L),
            "bantuanBerasKg" to bantuanBerasKg.coerceAtLeast(0.0),
            "catatan" to catatan.trim().take(250),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMustahik(mustahikId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(mustahikId.isNotBlank()) { "ID mustahik wajib diisi" }
        val docRef = db.collection("users").document(uid)
            .collection("mustahik").document(mustahikId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
