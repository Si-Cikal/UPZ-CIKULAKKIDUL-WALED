package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AmilProfile
import com.example.data.model.GolonganAsnaf
import com.example.data.model.JenisZakat
import com.example.data.model.MustahikRecord
import com.example.data.model.StatusPenyaluran
import com.example.data.model.StatusTransaksi
import com.example.data.model.ZakatTransaction
import com.example.data.repository.UpzRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DashboardSummary(
    val totalUangTerkumpul: Long = 0L,
    val totalBerasTerkumpulKg: Double = 0.0,
    val totalFitrahUang: Long = 0L,
    val totalMalUang: Long = 0L,
    val totalInfakFidyahUang: Long = 0L,
    val totalMuzakkiCount: Int = 0,
    val totalJiwaFitrah: Long = 0L,
    val totalTerverifikasi: Int = 0,
    val totalUangDisalurkan: Long = 0L,
    val totalBerasDisalurkanKg: Double = 0.0,
    val totalMustahikCount: Int = 0,
    val totalMustahikDisalurkan: Int = 0,
    val asnafCounts: Map<GolonganAsnaf, Int> = emptyMap(),
) {
    val sisaSaldoUang: Long get() = (totalUangTerkumpul - totalUangDisalurkan).coerceAtLeast(0L)
    val sisaStokBerasKg: Double get() = (totalBerasTerkumpulKg - totalBerasDisalurkanKg).coerceAtLeast(0.0)
    val rasioPenyaluranPersen: Int
        get() = if (totalMustahikCount == 0) 0 else ((totalMustahikDisalurkan * 100) / totalMustahikCount)
}

data class ZakatPrefill(
    val jenisZakat: String = JenisZakat.FITRAH_UANG.key,
    val jumlahJiwa: Long = 1L,
    val nominalUang: Long = 0L,
    val beratBerasKg: Double = 0.0,
    val catatan: String = "",
)

class UpzDashboardViewModel(
    private val repository: UpzRepository,
    private val currentUserId: String,
    private val displayName: String?,
) : ViewModel() {

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _zakatPrefill = MutableStateFlow<ZakatPrefill?>(null)
    val zakatPrefill: StateFlow<ZakatPrefill?> = _zakatPrefill.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultProfile(displayName)
                .onFailure { error ->
                    Log.w(TAG, "Gagal menyiapkan profil default amil", error)
                }
        }
    }

    val profileState: StateFlow<UiState<AmilProfile?>> = repository.observeProfile(currentUserId)
        .map<AmilProfile?, UiState<AmilProfile?>> { UiState.Success(it) }
        .catch { error ->
            val fsError = extractFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            val root = fsError ?: error
            Log.w(TAG, "Error observing profile", root)
            emit(UiState.Error(root.localizedMessage ?: "Gagal memuat profil petugas"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = UiState.Loading,
        )

    val transactionsState: StateFlow<UiState<List<ZakatTransaction>>> =
        repository.observeTransactions(currentUserId)
            .map<List<ZakatTransaction>, UiState<List<ZakatTransaction>>> { UiState.Success(it) }
            .catch { error ->
                val fsError = extractFirestoreError(error)
                if (fsError == null && error is CancellationException) throw error
                val root = fsError ?: error
                Log.w(TAG, "Error observing zakat transactions", root)
                emit(UiState.Error(root.localizedMessage ?: "Gagal memuat transaksi zakat"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading,
            )

    val mustahikState: StateFlow<UiState<List<MustahikRecord>>> =
        repository.observeMustahik(currentUserId)
            .map<List<MustahikRecord>, UiState<List<MustahikRecord>>> { UiState.Success(it) }
            .catch { error ->
                val fsError = extractFirestoreError(error)
                if (fsError == null && error is CancellationException) throw error
                val root = fsError ?: error
                Log.w(TAG, "Error observing mustahik", root)
                emit(UiState.Error(root.localizedMessage ?: "Gagal memuat data mustahik"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading,
            )

    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        transactionsState,
        mustahikState,
    ) { txUi, mustahikUi ->
        val transactions = (txUi as? UiState.Success)?.data.orEmpty()
        val mustahikList = (mustahikUi as? UiState.Success)?.data.orEmpty()

        val fitrahUang = transactions
            .filter { it.jenisZakat == JenisZakat.FITRAH_UANG.key }
            .sumOf { it.nominalUang }
        val malUang = transactions
            .filter { it.jenisZakat == JenisZakat.MAL.key }
            .sumOf { it.nominalUang }
        val infakFidyahUang = transactions
            .filter {
                it.jenisZakat == JenisZakat.INFAK_SEDEKAH.key ||
                    it.jenisZakat == JenisZakat.FIDYAH.key
            }
            .sumOf { it.nominalUang }

        val totalUangTerkumpul = transactions.sumOf { it.nominalUang }
        val totalBerasKg = transactions.sumOf { it.beratBerasKg }
        val totalJiwaFitrah = transactions
            .filter {
                it.jenisZakat == JenisZakat.FITRAH_UANG.key ||
                    it.jenisZakat == JenisZakat.FITRAH_BERAS.key
            }
            .sumOf { it.jumlahJiwa }
        val totalVerified = transactions.count { it.status == StatusTransaksi.TERVERIFIKASI.key }

        val disalurkan = mustahikList.filter {
            it.statusPenyaluran == StatusPenyaluran.DISALURKAN.key
        }
        val asnafMap = GolonganAsnaf.entries.associateWith { asnaf ->
            mustahikList.count { it.golonganAsnaf == asnaf.key }
        }

        DashboardSummary(
            totalUangTerkumpul = totalUangTerkumpul,
            totalBerasTerkumpulKg = totalBerasKg,
            totalFitrahUang = fitrahUang,
            totalMalUang = malUang,
            totalInfakFidyahUang = infakFidyahUang,
            totalMuzakkiCount = transactions.size,
            totalJiwaFitrah = totalJiwaFitrah,
            totalTerverifikasi = totalVerified,
            totalUangDisalurkan = disalurkan.sumOf { it.bantuanUang },
            totalBerasDisalurkanKg = disalurkan.sumOf { it.bantuanBerasKg },
            totalMustahikCount = mustahikList.size,
            totalMustahikDisalurkan = disalurkan.size,
            asnafCounts = asnafMap,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = DashboardSummary(),
    )

    fun clearMessage() {
        _actionMessage.value = null
    }

    fun setZakatPrefill(prefill: ZakatPrefill?) {
        _zakatPrefill.value = prefill
    }

    fun addZakatTransaction(
        namaMuzakki: String,
        rtRw: String,
        jenisZakat: String,
        jumlahJiwa: Long,
        nominalUang: Long,
        beratBerasKg: Double,
        metodePembayaran: String,
        catatan: String,
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.addTransaction(
                namaMuzakki = namaMuzakki,
                rtRw = rtRw,
                jenisZakat = jenisZakat,
                jumlahJiwa = jumlahJiwa,
                nominalUang = nominalUang,
                beratBerasKg = beratBerasKg,
                metodePembayaran = metodePembayaran,
                catatan = catatan,
            ).onSuccess {
                _actionMessage.value = "Transaksi zakat dari $namaMuzakki berhasil dicatat."
                onComplete()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Gagal mencatat transaksi zakat."
            }
            _isSaving.value = false
        }
    }

    fun updateZakatTransaction(tx: ZakatTransaction, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.updateTransaction(tx)
                .onSuccess {
                    _actionMessage.value = "Data kwitansi ${tx.nomorKwitansi} diperbarui."
                    onComplete()
                }
                .onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "Gagal memperbarui kwitansi."
                }
            _isSaving.value = false
        }
    }

    fun verifyZakatTransaction(txId: String, nomorKwitansi: String) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.verifyTransaction(txId)
                .onSuccess {
                    _actionMessage.value = "Kwitansi $nomorKwitansi resmi terverifikasi."
                }
                .onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "Gagal memverifikasi transaksi."
                }
            _isSaving.value = false
        }
    }

    fun deleteZakatTransaction(txId: String, namaMuzakki: String) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.deleteTransaction(txId)
                .onSuccess {
                    _actionMessage.value = "Catatan zakat $namaMuzakki dihapus."
                }
                .onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "Gagal menghapus transaksi."
                }
            _isSaving.value = false
        }
    }

    fun addMustahik(
        namaMustahik: String,
        golonganAsnaf: String,
        rtRw: String,
        bantuanUang: Long,
        bantuanBerasKg: Double,
        catatan: String,
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.addMustahik(
                namaMustahik = namaMustahik,
                golonganAsnaf = golonganAsnaf,
                rtRw = rtRw,
                bantuanUang = bantuanUang,
                bantuanBerasKg = bantuanBerasKg,
                catatan = catatan,
            ).onSuccess {
                _actionMessage.value = "Mustahik $namaMustahik berhasil didaftarkan."
                onComplete()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Gagal mendaftarkan mustahik."
            }
            _isSaving.value = false
        }
    }

    fun updateMustahik(record: MustahikRecord, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.updateMustahik(record)
                .onSuccess {
                    _actionMessage.value = "Data mustahik ${record.namaMustahik} diperbarui."
                    onComplete()
                }
                .onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "Gagal memperbarui data mustahik."
                }
            _isSaving.value = false
        }
    }

    fun distributeMustahik(
        mustahikId: String,
        namaMustahik: String,
        bantuanUang: Long,
        bantuanBerasKg: Double,
        catatan: String,
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.distributeMustahik(
                mustahikId = mustahikId,
                bantuanUang = bantuanUang,
                bantuanBerasKg = bantuanBerasKg,
                catatan = catatan,
            ).onSuccess {
                _actionMessage.value = "Bantuan untuk $namaMustahik telah disalurkan."
                onComplete()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Gagal menyalurkan bantuan."
            }
            _isSaving.value = false
        }
    }

    fun deleteMustahik(mustahikId: String, namaMustahik: String) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.deleteMustahik(mustahikId)
                .onSuccess {
                    _actionMessage.value = "Data mustahik $namaMustahik dihapus."
                }
                .onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "Gagal menghapus data mustahik."
                }
            _isSaving.value = false
        }
    }

    fun updateProfile(
        namaPetugas: String,
        jabatan: String,
        dusun: String,
        targetFitrahBerasKg: Double,
        targetZakatUang: Long,
        hargaBerasPerKg: Long,
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.updateProfile(
                namaPetugas = namaPetugas,
                jabatan = jabatan,
                dusun = dusun,
                targetFitrahBerasKg = targetFitrahBerasKg,
                targetZakatUang = targetZakatUang,
                hargaBerasPerKg = hargaBerasPerKg,
            ).onSuccess {
                _actionMessage.value = "Profil & target UPZ Desa Cikulak Kidul diperbarui."
                onComplete()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Gagal menyimpan profil UPZ."
            }
            _isSaving.value = false
        }
    }

    private fun extractFirestoreError(error: Throwable): FirebaseFirestoreException? {
        return generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
    }

    private companion object {
        const val TAG = "UpzDashboardVM"
        const val STOP_TIMEOUT_MS = 5000L
    }
}
