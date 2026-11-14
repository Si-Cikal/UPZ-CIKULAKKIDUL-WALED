package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class JenisZakat(
    val key: String,
    val label: String,
    val shortLabel: String,
    val deskripsi: String,
) {
    FITRAH_UANG("fitrah_uang", "Zakat Fitrah (Uang)", "Fitrah Uang", "2,5 kg beras dikonversi rupiah"),
    FITRAH_BERAS("fitrah_beras", "Zakat Fitrah (Beras)", "Fitrah Beras", "2,5 kg / 3,5 liter beras per jiwa"),
    MAL("mal", "Zakat Mal (Harta)", "Zakat Mal", "2,5% dari harta mencapai nisab & haul"),
    INFAK_SEDEKAH("infak_sedekah", "Infak & Sedekah", "Infak", "Donasi sukarela pemberdayaan warga"),
    FIDYAH("fidyah", "Fidyah", "Fidyah", "Pengganti puasa bagi yang berhalangan"),
    ;

    companion object {
        fun fromKey(key: String): JenisZakat = entries.firstOrNull { it.key == key } ?: FITRAH_UANG
    }
}

enum class MetodePembayaran(val key: String, val label: String) {
    TUNAI("tunai", "Tunai"),
    BERAS("beras", "Beras"),
    TRANSFER("transfer", "Transfer / QRIS"),
    ;

    companion object {
        fun fromKey(key: String): MetodePembayaran = entries.firstOrNull { it.key == key } ?: TUNAI
    }
}

enum class StatusTransaksi(val key: String, val label: String) {
    TERCATAT("tercatat", "Tercatat"),
    TERVERIFIKASI("terverifikasi", "Terverifikasi"),
    ;

    companion object {
        fun fromKey(key: String): StatusTransaksi = entries.firstOrNull { it.key == key } ?: TERCATAT
    }
}

enum class GolonganAsnaf(val key: String, val label: String, val deskripsi: String) {
    FAKIR("fakir", "Fakir", "Tidak memiliki harta dan penghasilan memadai"),
    MISKIN("miskin", "Miskin", "Memiliki penghasilan namun belum mencukupi kebutuhan dasar"),
    AMIL("amil", "Amil", "Petugas pengelola & pengumpul zakat UPZ desa"),
    MUALLAF("muallaf", "Muallaf", "Baru memeluk Islam yang perlu penguatan"),
    RIQAB("riqab", "Riqab", "Hamba sahaya / pembebasan dari belenggu"),
    GHARIMIN("gharimin", "Gharimin", "Terlilit utang untuk kebutuhan mendesak & halal"),
    FISABILILLAH("fisabilillah", "Fisabilillah", "Pejuang di jalan Allah, dakwah & pendidikan"),
    IBNU_SABIL("ibnu_sabil", "Ibnu Sabil", "Musafir yang kehabisan bekal dalam perjalanan baik"),
    ;

    companion object {
        fun fromKey(key: String): GolonganAsnaf = entries.firstOrNull { it.key == key } ?: FAKIR
    }
}

enum class StatusPenyaluran(val key: String, val label: String) {
    TERDAFTAR("terdaftar", "Terdaftar"),
    DISALURKAN("disalurkan", "Disalurkan"),
    ;

    companion object {
        fun fromKey(key: String): StatusPenyaluran = entries.firstOrNull { it.key == key } ?: TERDAFTAR
    }
}

object WilayahCikulakKidul {
    const val NAMA_DESA_LENGKAP = "Desa Cikulak Kidul, Kec. Waled, Kab. Cirebon"
    const val NAMA_UPZ = "UPZ Desa Cikulak Kidul"
    const val KODE_WILAYAH = "Kec. Waled • Kab. Cirebon"
    val daftarDusun = listOf(
        "Dusun I (Manis)",
        "Dusun II (Pahing)",
        "Dusun III (Pon)",
        "Dusun IV (Wage)",
        "Dusun V (Kliwon)",
    )
    val daftarJabatan = listOf(
        "Ketua UPZ Desa",
        "Sekretaris UPZ",
        "Bendahara UPZ",
        "Petugas Amil Dusun",
        "Relawan Pengumpul Zakat",
    )
}

@IgnoreExtraProperties
data class AmilProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val namaPetugas: String = "",
    val jabatan: String = "Petugas Amil Dusun",
    val dusun: String = "Dusun I (Manis)",
    val desa: String = WilayahCikulakKidul.NAMA_DESA_LENGKAP,
    val targetFitrahBerasKg: Double = 2500.0,
    val targetZakatUang: Long = 50_000_000L,
    val hargaBerasPerKg: Long = 14_000L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class ZakatTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val nomorKwitansi: String = "",
    val namaMuzakki: String = "",
    val rtRw: String = "",
    val jenisZakat: String = JenisZakat.FITRAH_UANG.key,
    val jumlahJiwa: Long = 1L,
    val nominalUang: Long = 0L,
    val beratBerasKg: Double = 0.0,
    val metodePembayaran: String = MetodePembayaran.TUNAI.key,
    val catatan: String = "",
    val status: String = StatusTransaksi.TERCATAT.key,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class MustahikRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val namaMustahik: String = "",
    val golonganAsnaf: String = GolonganAsnaf.FAKIR.key,
    val rtRw: String = "",
    val statusPenyaluran: String = StatusPenyaluran.TERDAFTAR.key,
    val bantuanUang: Long = 0L,
    val bantuanBerasKg: Double = 0.0,
    val catatan: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)
