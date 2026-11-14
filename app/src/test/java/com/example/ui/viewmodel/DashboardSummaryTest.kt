package com.example.ui.viewmodel

import com.example.data.model.AmilProfile
import com.example.data.model.GolonganAsnaf
import com.example.data.model.JenisZakat
import com.example.data.model.MetodePembayaran
import com.example.data.model.MustahikRecord
import com.example.data.model.StatusPenyaluran
import com.example.data.model.StatusTransaksi
import com.example.data.model.ZakatTransaction
import com.example.ui.util.buildCsvReportText
import com.example.ui.util.buildDigitalReceiptText
import com.example.ui.util.buildMustahikDistributionReceiptText
import com.example.ui.util.buildVillageReportText
import com.example.ui.util.formatKg
import com.example.ui.util.formatRupiah
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardSummaryTest {

    @Test
    fun summary_calculatesRemainingBalancesAndRatioCorrectly() {
        val summary = DashboardSummary(
            totalUangTerkumpul = 10_000_000L,
            totalBerasTerkumpulKg = 250.0,
            totalUangDisalurkan = 3_500_000L,
            totalBerasDisalurkanKg = 75.5,
            totalMustahikCount = 10,
            totalMustahikDisalurkan = 4,
            asnafCounts = mapOf(GolonganAsnaf.FAKIR to 4, GolonganAsnaf.MISKIN to 6),
        )

        assertEquals(6_500_000L, summary.sisaSaldoUang)
        assertEquals(174.5, summary.sisaStokBerasKg, 0.001)
        assertEquals(40, summary.rasioPenyaluranPersen)
    }

    @Test
    fun formatters_formatIndonesianCurrencyAndKgCleanly() {
        assertEquals("Rp 140.000", formatRupiah(140_000L))
        assertEquals("10 kg", formatKg(10.0))
        assertEquals("2,5 kg", formatKg(2.5))
    }

    @Test
    fun exporters_generateReceiptAndVillageReportWithCikulakKidulIdentity() {
        val tx = ZakatTransaction(
            id = "tx1",
            nomorKwitansi = "UPZ-CK-261114-ABCD",
            namaMuzakki = "H. Ahmad",
            rtRw = "RT 002 / RW 001 - Dusun I (Manis)",
            jenisZakat = JenisZakat.FITRAH_UANG.key,
            jumlahJiwa = 4L,
            nominalUang = 140_000L,
            beratBerasKg = 0.0,
            metodePembayaran = MetodePembayaran.TUNAI.key,
            status = StatusTransaksi.TERVERIFIKASI.key,
        )
        val receipt = buildDigitalReceiptText(tx, "Ust. Fauzi")
        assertTrue(receipt.contains("UPZ-CK-261114-ABCD"))
        assertTrue(receipt.contains("Desa Cikulak Kidul"))
        assertTrue(receipt.contains("Rp 140.000"))

        val profile = AmilProfile(namaPetugas = "Ust. Fauzi", jabatan = "Ketua UPZ Desa")
        val summary = DashboardSummary(totalUangTerkumpul = 140_000L, totalMuzakkiCount = 1)
        val report = buildVillageReportText(profile, summary, listOf(tx), emptyList())
        assertTrue(report.contains("UPZ) DESA CIKULAK KIDUL"))
        assertTrue(report.contains("Dusun I (Manis)"))

        val csv = buildCsvReportText(listOf(tx), emptyList())
        assertTrue(csv.contains("UPZ DESA CIKULAK KIDUL"))
        assertTrue(csv.contains("\"UPZ-CK-261114-ABCD\",\"H. Ahmad\""))

        val mustahik = MustahikRecord(
            id = "m1",
            namaMustahik = "Ibu Aminah",
            golonganAsnaf = GolonganAsnaf.FAKIR.key,
            rtRw = "RT 003 / RW 001 - Dusun II (Pahing)",
            statusPenyaluran = StatusPenyaluran.DISALURKAN.key,
            bantuanUang = 300_000L,
            bantuanBerasKg = 5.0,
        )
        val mustahikSlip = buildMustahikDistributionReceiptText(mustahik, "Ust. Fauzi")
        assertTrue(mustahikSlip.contains("TANDA TERIMA PENYALURAN ZAKAT"))
        assertTrue(mustahikSlip.contains("Ibu Aminah"))
        assertTrue(mustahikSlip.contains("Rp 300.000"))
    }
}
