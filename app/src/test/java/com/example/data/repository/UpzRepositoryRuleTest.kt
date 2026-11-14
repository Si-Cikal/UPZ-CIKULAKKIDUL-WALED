package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.GolonganAsnaf
import com.example.data.model.JenisZakat
import com.example.data.model.MetodePembayaran
import com.example.data.model.StatusPenyaluran
import com.example.data.model.StatusTransaksi
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpzRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureDefaultProfile_authenticated_createsAndObservesProfile() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = UpzRepository(firestore, auth)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureDefaultProfile("Ust. Ahmad Fauzi")
        }
        assertTrue("Expected ensureDefaultProfile to succeed", result.isSuccess)

        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeProfile(uid).first { it != null }
        }
        assertNotNull(profile)
        assertEquals(uid, profile?.userId)
        assertEquals("Desa Cikulak Kidul, Kec. Waled, Kab. Cirebon", profile?.desa)
    }

    @Test
    fun addAndVerifyTransaction_authenticated_succeedsAndLocksUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = UpzRepository(firestore, auth)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addTransaction(
                namaMuzakki = "H. Abdullah",
                rtRw = "RT 002 / RW 001 - Dusun Manis",
                jenisZakat = JenisZakat.FITRAH_UANG.key,
                jumlahJiwa = 4L,
                nominalUang = 140_000L,
                beratBerasKg = 0.0,
                metodePembayaran = MetodePembayaran.TUNAI.key,
                catatan = "Zakat fitrah sekeluarga"
            )
        }
        assertTrue("Expected addTransaction to succeed", createResult.isSuccess)
        val txId = createResult.getOrThrow()
        assertTrue(txId.isNotBlank())

        val list = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTransactions(uid).first { items -> items.any { it.id == txId } }
        }
        val created = list.first { it.id == txId }
        assertEquals(StatusTransaksi.TERCATAT.key, created.status)

        val verifyResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.verifyTransaction(txId)
        }
        assertTrue("Expected verifyTransaction to succeed", verifyResult.isSuccess)

        // Terminal lock check: updating after verified must fail with PERMISSION_DENIED
        val lockedUpdate = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateTransaction(created.copy(nominalUang = 200_000L))
        }
        assertTrue("Expected update after verification to fail", lockedUpdate.isFailure)
        val fsError = lockedUpdate.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
    }

    @Test
    fun addAndDistributeMustahik_authenticated_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = UpzRepository(firestore, auth)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addMustahik(
                namaMustahik = "Ibu Aminah",
                golonganAsnaf = GolonganAsnaf.FAKIR.key,
                rtRw = "RT 004 / RW 002 - Dusun Pahing",
                bantuanUang = 250_000L,
                bantuanBerasKg = 5.0,
                catatan = "Lansia dhuafa"
            )
        }
        assertTrue("Expected addMustahik to succeed", createResult.isSuccess)
        val mustahikId = createResult.getOrThrow()

        val distributeResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.distributeMustahik(
                mustahikId = mustahikId,
                bantuanUang = 300_000L,
                bantuanBerasKg = 7.5,
                catatan = "Sudah diserahkan langsung"
            )
        }
        assertTrue("Expected distributeMustahik to succeed", distributeResult.isSuccess)

        val items = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMustahik(uid).first { list ->
                list.any { it.id == mustahikId && it.statusPenyaluran == StatusPenyaluran.DISALURKAN.key }
            }
        }
        assertTrue(items.any { it.id == mustahikId })
    }

    @Test
    fun observeTransactions_unauthenticated_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = UpzRepository(firestore, auth)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeTransactions(aliceUid).first()
            }
        } catch (t: Throwable) {
            caught = t
        }
        assertNotNull("Expected unauthenticated observeTransactions to throw", caught)
        val fsError = caught?.unwrapFirestoreException()
        assertNotNull("Expected nested FirebaseFirestoreException", fsError)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
    }

    @Test
    fun crossUserRead_bobCannotReadAliceTransaction() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = UpzRepository(firestore, auth)
        val txId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addTransaction(
                namaMuzakki = "Bpk. Ridwan",
                rtRw = "RT 001 / RW 001",
                jenisZakat = JenisZakat.MAL.key,
                jumlahJiwa = 0L,
                nominalUang = 1_000_000L,
                beratBerasKg = 0.0,
                metodePembayaran = MetodePembayaran.TRANSFER.key,
                catatan = "Zakat penghasilan"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        var caught: Throwable? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("zakat_transactions").document(txId)
                    .get()
                    .await()
            }
        } catch (t: Throwable) {
            caught = t
        }
        assertNotNull("Expected cross-user read to fail", caught)
        val fsError = caught?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "amil_alice@cikulakkidul.id"
        const val BOB_EMAIL = "amil_bob@cikulakkidul.id"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
