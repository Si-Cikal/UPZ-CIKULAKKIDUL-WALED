package com.example.ui.util

import android.content.Context
import android.content.Intent
import com.example.data.model.AmilProfile
import com.example.data.model.GolonganAsnaf
import com.example.data.model.JenisZakat
import com.example.data.model.MetodePembayaran
import com.example.data.model.MustahikRecord
import com.example.data.model.StatusPenyaluran
import com.example.data.model.StatusTransaksi
import com.example.data.model.WilayahCikulakKidul
import com.example.data.model.ZakatTransaction
import com.example.ui.viewmodel.DashboardSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun buildDigitalReceiptText(
    tx: ZakatTransaction,
    officerName: String,
): String {
    val jenis = JenisZakat.fromKey(tx.jenisZakat).label
    val metode = MetodePembayaran.fromKey(tx.metodePembayaran).label
    val status = StatusTransaksi.fromKey(tx.status).label
    val waktu = formatTimestamp(tx.createdAt)

    return buildString {
        appendLine("====================================")
        appendLine("KWITANSI DIGITAL RESMI")
        appendLine("UNIT PENGUMPUL ZAKAT (UPZ)")
        appendLine(WilayahCikulakKidul.NAMA_DESA_LENGKAP)
        appendLine("====================================")
        appendLine("No. Kwitansi : ${tx.nomorKwitansi}")
        appendLine("Tanggal      : $waktu")
        appendLine("Status       : $status")
        appendLine("------------------------------------")
        appendLine("Muzakki      : ${tx.namaMuzakki}")
        appendLine("Alamat       : ${tx.rtRw}")
        appendLine("Jenis ZIS    : $jenis")
        if (tx.jumlahJiwa > 0L) {
            appendLine("Jumlah Jiwa  : ${tx.jumlahJiwa} Jiwa")
        }
        if (tx.nominalUang > 0L) {
            appendLine("Nominal Uang : ${formatRupiah(tx.nominalUang)}")
        }
        if (tx.beratBerasKg > 0.0) {
            appendLine("Berat Beras  : ${formatKg(tx.beratBerasKg)}")
        }
        appendLine("Metode Bayar : $metode")
        if (tx.catatan.isNotBlank()) {
            appendLine("Catatan      : ${tx.catatan}")
        }
        appendLine("Petugas Amil : $officerName")
        appendLine("------------------------------------")
        appendLine("Doa Amil untuk Muzakki:")
        appendLine("\"Ajarakallahu fima a'thaita wa baraka fima abqaita wa ja'alahu laka thahuran.\"")
        appendLine("(Semoga Allah memberi pahala atas apa yang engkau berikan, memberkahi harta yang tersisa, dan menjadikannya penyuci bagimu.)")
        appendLine("====================================")
    }
}

fun buildMustahikDistributionReceiptText(
    record: MustahikRecord,
    officerName: String,
): String {
    val asnaf = GolonganAsnaf.fromKey(record.golonganAsnaf).label
    val status = StatusPenyaluran.fromKey(record.statusPenyaluran).label
    val waktu = formatTimestamp(record.updatedAt ?: record.createdAt)

    return buildString {
        appendLine("====================================")
        appendLine("TANDA TERIMA PENYALURAN ZAKAT")
        appendLine("UNIT PENGUMPUL ZAKAT (UPZ)")
        appendLine(WilayahCikulakKidul.NAMA_DESA_LENGKAP)
        appendLine("====================================")
        appendLine("Nama Mustahik : ${record.namaMustahik}")
        appendLine("Golongan Asnaf: $asnaf")
        appendLine("Alamat/Dusun  : ${record.rtRw}")
        appendLine("Status        : $status")
        appendLine("Waktu         : $waktu")
        appendLine("------------------------------------")
        if (record.bantuanUang > 0L) {
            appendLine("Santunan Uang : ${formatRupiah(record.bantuanUang)}")
        }
        if (record.bantuanBerasKg > 0.0) {
            appendLine("Bantuan Beras : ${formatKg(record.bantuanBerasKg)}")
        }
        if (record.catatan.isNotBlank()) {
            appendLine("Keterangan    : ${record.catatan}")
        }
        appendLine("Petugas Amil  : $officerName")
        appendLine("------------------------------------")
        appendLine("Amanah, Transparan, Tepat Sasaran")
        appendLine("====================================")
    }
}

fun buildVillageReportText(
    profile: AmilProfile?,
    summary: DashboardSummary,
    transactions: List<ZakatTransaction>,
    mustahikList: List<MustahikRecord>,
): String {
    val tanggalCetak = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        .format(Date())
    val petugas = profile?.namaPetugas?.ifBlank { "Petugas Amil UPZ" } ?: "Petugas Amil UPZ"
    val jabatan = profile?.jabatan ?: "Petugas Amil UPZ"

    return buildString {
        appendLine("LAPORAN PENGHIMPUNAN & PENYALURAN ZIS")
        appendLine("UNIT PENGUMPUL ZAKAT (UPZ) DESA CIKULAK KIDUL")
        appendLine("Kecamatan Waled, Kabupaten Cirebon, Jawa Barat")
        appendLine("Dicetak: $tanggalCetak WIB")
        appendLine("Petugas: $petugas ($jabatan)")
        appendLine("================================================")
        appendLine("A. RINGKASAN PENGHIMPUNAN")
        appendLine("• Total Muzakki / Transaksi : ${summary.totalMuzakkiCount} transaksi (${summary.totalTerverifikasi} terverifikasi)")
        appendLine("• Total Dana ZIS Terhimpun  : ${formatRupiah(summary.totalUangTerkumpul)}")
        appendLine("  - Zakat Fitrah (Uang)     : ${formatRupiah(summary.totalFitrahUang)}")
        appendLine("  - Zakat Mal (Harta)       : ${formatRupiah(summary.totalMalUang)}")
        appendLine("  - Infak, Sedekah & Fidyah : ${formatRupiah(summary.totalInfakFidyahUang)}")
        appendLine("• Total Beras Fitrah        : ${formatKg(summary.totalBerasTerkumpulKg)} (${summary.totalJiwaFitrah} jiwa)")
        appendLine()
        appendLine("B. RINGKASAN PENYALURAN MUSTAHIK")
        appendLine("• Total Mustahik Terdaftar  : ${summary.totalMustahikCount} penerima")
        appendLine("• Sudah Disalurkan          : ${summary.totalMustahikDisalurkan} penerima (${summary.rasioPenyaluranPersen}%)")
        appendLine("• Dana ZIS Disalurkan       : ${formatRupiah(summary.totalUangDisalurkan)}")
        appendLine("• Beras Disalurkan          : ${formatKg(summary.totalBerasDisalurkanKg)}")
        appendLine("• Sisa Saldo Kas ZIS        : ${formatRupiah(summary.sisaSaldoUang)}")
        appendLine("• Sisa Stok Beras           : ${formatKg(summary.sisaStokBerasKg)}")
        appendLine()
        appendLine("C. REKAPITULASI PER DUSUN")
        WilayahCikulakKidul.daftarDusun.forEach { dusun ->
            val shortKey = dusun.substringAfter("(").substringBefore(")").lowercase()
            val dusunTx = transactions.filter {
                it.rtRw.contains(dusun, ignoreCase = true) ||
                    it.rtRw.contains(shortKey, ignoreCase = true)
            }
            val uang = dusunTx.sumOf { it.nominalUang }
            val beras = dusunTx.sumOf { it.beratBerasKg }
            appendLine("• $dusun: ${dusunTx.size} setoran | ${formatRupiah(uang)} | ${formatKg(beras)}")
        }
        appendLine()
        appendLine("D. SEBARAN MUSTAHIK (8 ASNAF)")
        GolonganAsnaf.entries.forEach { asnaf ->
            val count = mustahikList.count { it.golonganAsnaf == asnaf.key }
            val sent = mustahikList.count {
                it.golonganAsnaf == asnaf.key &&
                    it.statusPenyaluran == StatusPenyaluran.DISALURKAN.key
            }
            if (count > 0) {
                appendLine("• ${asnaf.label}: $sent/$count disalurkan")
            }
        }
        appendLine("================================================")
        appendLine("Amanah, Transparan, Akuntabel • UPZ Desa Cikulak Kidul")
    }
}

fun buildCsvReportText(
    transactions: List<ZakatTransaction>,
    mustahikList: List<MustahikRecord>,
): String {
    fun csvEscape(raw: String): String {
        val cleaned = raw.replace("\r", " ").replace("\n", " ").replace("\"", "\"\"")
        return "\"$cleaned\""
    }

    return buildString {
        appendLine("UPZ DESA CIKULAK KIDUL - KEC. WALED - KAB. CIREBON")
        appendLine()
        appendLine("[PENERIMAAN ZAKAT, INFAK & FIDYAH]")
        appendLine("No Kwitansi,Nama Muzakki,Alamat/Dusun,Jenis ZIS,Jiwa,Nominal Uang (Rp),Berat Beras (kg),Metode,Status,Waktu")
        transactions.forEach { tx ->
            appendLine(
                listOf(
                    csvEscape(tx.nomorKwitansi),
                    csvEscape(tx.namaMuzakki),
                    csvEscape(tx.rtRw),
                    csvEscape(JenisZakat.fromKey(tx.jenisZakat).label),
                    tx.jumlahJiwa.toString(),
                    tx.nominalUang.toString(),
                    tx.beratBerasKg.toString(),
                    csvEscape(MetodePembayaran.fromKey(tx.metodePembayaran).label),
                    csvEscape(StatusTransaksi.fromKey(tx.status).label),
                    csvEscape(formatTimestamp(tx.createdAt)),
                ).joinToString(",")
            )
        }
        appendLine()
        appendLine("[DAFTAR MUSTAHIK & PENYALURAN]")
        appendLine("Nama Mustahik,Golongan Asnaf,Alamat/Dusun,Status Penyaluran,Santunan Uang (Rp),Bantuan Beras (kg),Catatan")
        mustahikList.forEach { m ->
            appendLine(
                listOf(
                    csvEscape(m.namaMustahik),
                    csvEscape(GolonganAsnaf.fromKey(m.golonganAsnaf).label),
                    csvEscape(m.rtRw),
                    csvEscape(StatusPenyaluran.fromKey(m.statusPenyaluran).label),
                    m.bantuanUang.toString(),
                    m.bantuanBerasKg.toString(),
                    csvEscape(m.catatan),
                ).joinToString(",")
            )
        }
    }
}

fun sharePlainText(
    context: Context,
    subject: String,
    text: String,
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(sendIntent, subject)
    context.startActivity(chooser)
}
