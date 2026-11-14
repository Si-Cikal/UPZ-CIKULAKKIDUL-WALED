package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AmilProfile
import com.example.data.model.GolonganAsnaf
import com.example.data.model.JenisZakat
import com.example.data.model.MetodePembayaran
import com.example.data.model.WilayahCikulakKidul
import com.example.data.model.ZakatTransaction
import com.example.ui.viewmodel.ZakatPrefill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditZakatSheet(
    existingTx: ZakatTransaction?,
    prefill: ZakatPrefill?,
    hargaBerasPerKg: Long,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmitNew: (
        namaMuzakki: String,
        rtRw: String,
        jenisZakat: String,
        jumlahJiwa: Long,
        nominalUang: Long,
        beratBerasKg: Double,
        metodePembayaran: String,
        catatan: String,
    ) -> Unit,
    onSubmitEdit: (ZakatTransaction) -> Unit,
) {
    var namaMuzakki by remember { mutableStateOf(existingTx?.namaMuzakki ?: "") }
    var selectedDusun by remember { mutableStateOf(WilayahCikulakKidul.daftarDusun.first()) }
    var rtRwInput by remember {
        mutableStateOf(existingTx?.rtRw ?: "RT 001 / RW 001 - ${WilayahCikulakKidul.daftarDusun.first()}")
    }
    var jenisZakat by remember {
        mutableStateOf(
            existingTx?.jenisZakat
                ?: prefill?.jenisZakat
                ?: JenisZakat.FITRAH_UANG.key
        )
    }
    var jumlahJiwa by remember {
        mutableLongStateOf(
            existingTx?.jumlahJiwa
                ?: prefill?.jumlahJiwa
                ?: 1L
        )
    }
    var nominalUangText by remember {
        val initUang = existingTx?.nominalUang
            ?: prefill?.nominalUang
            ?: if (jenisZakat == JenisZakat.FITRAH_UANG.key) ((2.5 * hargaBerasPerKg).toLong()) else 0L
        mutableStateOf(initUang.toString())
    }
    var beratBerasText by remember {
        val initBeras = existingTx?.beratBerasKg
            ?: prefill?.beratBerasKg
            ?: 0.0
        mutableStateOf(if (initBeras > 0.0) initBeras.toString() else "0")
    }
    var metodePembayaran by remember {
        mutableStateOf(
            existingTx?.metodePembayaran
                ?: if (jenisZakat == JenisZakat.FITRAH_BERAS.key) {
                    MetodePembayaran.BERAS.key
                } else {
                    MetodePembayaran.TUNAI.key
                }
        )
    }
    var catatan by remember { mutableStateOf(existingTx?.catatan ?: prefill?.catatan ?: "") }
    var formError by remember { mutableStateOf<String?>(null) }

    fun recalculateFitrah(newJenis: String, newJiwa: Long) {
        if (newJenis == JenisZakat.FITRAH_UANG.key) {
            nominalUangText = (newJiwa * 2.5 * hargaBerasPerKg).toLong().toString()
            beratBerasText = "0"
            metodePembayaran = MetodePembayaran.TUNAI.key
        } else if (newJenis == JenisZakat.FITRAH_BERAS.key) {
            beratBerasText = (newJiwa * 2.5).toString()
            nominalUangText = "0"
            metodePembayaran = MetodePembayaran.BERAS.key
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (existingTx == null) "Catat Penerimaan ZIS" else "Ubah Kwitansi ${existingTx.nomorKwitansi}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Jenis Penghimpunan",
                style = MaterialTheme.typography.labelLarge,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                JenisZakat.entries.forEach { item ->
                    FilterChip(
                        selected = jenisZakat == item.key,
                        onClick = {
                            jenisZakat = item.key
                            if (item == JenisZakat.FITRAH_UANG || item == JenisZakat.FITRAH_BERAS) {
                                if (jumlahJiwa <= 0L) jumlahJiwa = 1L
                                recalculateFitrah(item.key, jumlahJiwa)
                            }
                        },
                        label = { Text(item.shortLabel) },
                    )
                }
            }

            OutlinedTextField(
                value = namaMuzakki,
                onValueChange = { namaMuzakki = it },
                label = { Text("Nama Muzakki / Kepala Keluarga") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_nama_muzakki"),
            )

            Text(
                text = "Pilih Dusun (Desa Cikulak Kidul)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WilayahCikulakKidul.daftarDusun.forEach { dusun ->
                    FilterChip(
                        selected = selectedDusun == dusun,
                        onClick = {
                            selectedDusun = dusun
                            rtRwInput = "RT 001 / RW 001 - $dusun"
                        },
                        label = { Text(dusun) },
                    )
                }
            }

            OutlinedTextField(
                value = rtRwInput,
                onValueChange = { rtRwInput = it },
                label = { Text("RT / RW & Alamat Dusun") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (jenisZakat == JenisZakat.FITRAH_UANG.key || jenisZakat == JenisZakat.FITRAH_BERAS.key) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Jumlah Jiwa (otomatis 2,5 kg/jiwa)",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                if (jumlahJiwa > 1L) {
                                    jumlahJiwa--
                                    recalculateFitrah(jenisZakat, jumlahJiwa)
                                }
                            },
                        ) { Text("-") }
                        Text(
                            text = "$jumlahJiwa",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        FilledTonalIconButton(
                            onClick = {
                                if (jumlahJiwa < 50L) {
                                    jumlahJiwa++
                                    recalculateFitrah(jenisZakat, jumlahJiwa)
                                }
                            },
                        ) { Text("+") }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = nominalUangText,
                    onValueChange = { nominalUangText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Uang (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_nominal_uang"),
                )
                OutlinedTextField(
                    value = beratBerasText,
                    onValueChange = { beratBerasText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Berat Beras (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_berat_beras"),
                )
            }

            Text(text = "Metode Pembayaran", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetodePembayaran.entries.forEach { m ->
                    FilterChip(
                        selected = metodePembayaran == m.key,
                        onClick = { metodePembayaran = m.key },
                        label = { Text(m.label) },
                    )
                }
            }

            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                label = { Text("Catatan Tambahan (Opsional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (formError != null) {
                Text(
                    text = formError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        val nominal = nominalUangText.toLongOrNull() ?: 0L
                        val beras = beratBerasText.toDoubleOrNull() ?: 0.0
                        when {
                            namaMuzakki.trim().length < 2 ->
                                formError = "Nama muzakki minimal 2 karakter."
                            rtRwInput.trim().length < 2 ->
                                formError = "Isi alamat RT/RW atau Dusun."
                            nominal <= 0L && beras <= 0.0 ->
                                formError = "Nominal uang atau berat beras harus lebih dari 0."
                            else -> {
                                formError = null
                                if (existingTx == null) {
                                    onSubmitNew(
                                        namaMuzakki,
                                        rtRwInput,
                                        jenisZakat,
                                        jumlahJiwa,
                                        nominal,
                                        beras,
                                        metodePembayaran,
                                        catatan,
                                    )
                                } else {
                                    onSubmitEdit(
                                        existingTx.copy(
                                            namaMuzakki = namaMuzakki,
                                            rtRw = rtRwInput,
                                            jenisZakat = jenisZakat,
                                            jumlahJiwa = jumlahJiwa,
                                            nominalUang = nominal,
                                            beratBerasKg = beras,
                                            metodePembayaran = metodePembayaran,
                                            catatan = catatan,
                                        )
                                    )
                                }
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("submit_zakat_button"),
                ) {
                    Text(if (isSaving) "Menyimpan..." else "Simpan Kwitansi")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMustahikSheet(
    existingMustahik: com.example.data.model.MustahikRecord?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmitNew: (
        namaMustahik: String,
        golonganAsnaf: String,
        rtRw: String,
        bantuanUang: Long,
        bantuanBerasKg: Double,
        catatan: String,
    ) -> Unit,
    onSubmitEdit: (com.example.data.model.MustahikRecord) -> Unit,
) {
    var namaMustahik by remember { mutableStateOf(existingMustahik?.namaMustahik ?: "") }
    var golonganAsnaf by remember { mutableStateOf(existingMustahik?.golonganAsnaf ?: GolonganAsnaf.FAKIR.key) }
    var selectedDusun by remember {
        val matched = existingMustahik?.rtRw?.let { rt ->
            WilayahCikulakKidul.daftarDusun.firstOrNull { d ->
                rt.contains(d, ignoreCase = true) ||
                    rt.contains(d.substringAfter("(").substringBefore(")"), ignoreCase = true)
            }
        }
        mutableStateOf(matched ?: WilayahCikulakKidul.daftarDusun.first())
    }
    var rtRw by remember {
        mutableStateOf(existingMustahik?.rtRw ?: "RT 001 / RW 001 - ${WilayahCikulakKidul.daftarDusun.first()}")
    }
    var bantuanUangText by remember {
        mutableStateOf((existingMustahik?.bantuanUang ?: 250000L).toString())
    }
    var bantuanBerasText by remember {
        val initBeras = existingMustahik?.bantuanBerasKg ?: 5.0
        mutableStateOf(if (initBeras % 1.0 == 0.0) initBeras.toLong().toString() else initBeras.toString())
    }
    var catatan by remember { mutableStateOf(existingMustahik?.catatan ?: "") }
    var formError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (existingMustahik == null) "Tambah Mustahik Desa" else "Ubah Data Mustahik",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            OutlinedTextField(
                value = namaMustahik,
                onValueChange = { namaMustahik = it },
                label = { Text("Nama Penerima Manfaat (Mustahik)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_nama_mustahik"),
            )

            Text("Golongan Asnaf", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GolonganAsnaf.entries.forEach { asnaf ->
                    FilterChip(
                        selected = golonganAsnaf == asnaf.key,
                        onClick = { golonganAsnaf = asnaf.key },
                        label = { Text(asnaf.label) },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WilayahCikulakKidul.daftarDusun.forEach { dusun ->
                    FilterChip(
                        selected = selectedDusun == dusun,
                        onClick = {
                            selectedDusun = dusun
                            rtRw = "RT 001 / RW 001 - $dusun"
                        },
                        label = { Text(dusun) },
                    )
                }
            }

            OutlinedTextField(
                value = rtRw,
                onValueChange = { rtRw = it },
                label = { Text("Alamat RT / RW & Dusun") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = bantuanUangText,
                    onValueChange = { bantuanUangText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Rencana Uang (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = bantuanBerasText,
                    onValueChange = { bantuanBerasText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Rencana Beras (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                label = { Text("Keterangan / Kondisi Keluarga") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (formError != null) {
                Text(
                    text = formError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        if (namaMustahik.trim().length < 2) {
                            formError = "Nama mustahik minimal 2 karakter."
                        } else if (rtRw.trim().length < 2) {
                            formError = "Isi alamat RT/RW."
                        } else {
                            formError = null
                            val uang = bantuanUangText.toLongOrNull() ?: 0L
                            val beras = bantuanBerasText.toDoubleOrNull() ?: 0.0
                            if (existingMustahik == null) {
                                onSubmitNew(
                                    namaMustahik,
                                    golonganAsnaf,
                                    rtRw,
                                    uang,
                                    beras,
                                    catatan,
                                )
                            } else {
                                onSubmitEdit(
                                    existingMustahik.copy(
                                        namaMustahik = namaMustahik,
                                        golonganAsnaf = golonganAsnaf,
                                        rtRw = rtRw,
                                        bantuanUang = uang,
                                        bantuanBerasKg = beras,
                                        catatan = catatan,
                                    )
                                )
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("submit_mustahik_button"),
                ) {
                    Text(
                        when {
                            isSaving -> "Menyimpan..."
                            existingMustahik == null -> "Daftarkan"
                            else -> "Simpan Perubahan"
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsSheet(
    profile: AmilProfile?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        namaPetugas: String,
        jabatan: String,
        dusun: String,
        targetBerasKg: Double,
        targetUang: Long,
        hargaBerasPerKg: Long,
    ) -> Unit,
) {
    var namaPetugas by remember(profile) { mutableStateOf(profile?.namaPetugas ?: "Petugas Amil UPZ") }
    var jabatan by remember(profile) { mutableStateOf(profile?.jabatan ?: WilayahCikulakKidul.daftarJabatan.first()) }
    var dusun by remember(profile) { mutableStateOf(profile?.dusun ?: WilayahCikulakKidul.daftarDusun.first()) }
    var hargaBerasText by remember(profile) { mutableStateOf((profile?.hargaBerasPerKg ?: 14_000L).toString()) }
    var targetBerasText by remember(profile) { mutableStateOf((profile?.targetFitrahBerasKg?.toLong() ?: 2500L).toString()) }
    var targetUangText by remember(profile) { mutableStateOf((profile?.targetZakatUang ?: 50_000_000L).toString()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Pengaturan Amil & Target Desa",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = WilayahCikulakKidul.NAMA_DESA_LENGKAP,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = namaPetugas,
                onValueChange = { namaPetugas = it },
                label = { Text("Nama Petugas Amil") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Jabatan di UPZ", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WilayahCikulakKidul.daftarJabatan.forEach { item ->
                    FilterChip(
                        selected = jabatan == item,
                        onClick = { jabatan = item },
                        label = { Text(item) },
                    )
                }
            }

            Text("Wilayah Tugas Dusun", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WilayahCikulakKidul.daftarDusun.forEach { item ->
                    FilterChip(
                        selected = dusun == item,
                        onClick = { dusun = item },
                        label = { Text(item) },
                    )
                }
            }

            OutlinedTextField(
                value = hargaBerasText,
                onValueChange = { hargaBerasText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Standar Harga Beras Desa / kg (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = targetUangText,
                    onValueChange = { targetUangText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Target Dana ZIS (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = targetBerasText,
                    onValueChange = { targetBerasText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Target Beras (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        onSave(
                            namaPetugas.ifBlank { "Petugas Amil UPZ" },
                            jabatan,
                            dusun,
                            targetBerasText.toDoubleOrNull() ?: 2500.0,
                            targetUangText.toLongOrNull() ?: 50_000_000L,
                            (hargaBerasText.toLongOrNull() ?: 14_000L).coerceIn(1000L, 500_000L),
                        )
                    },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (isSaving) "Menyimpan..." else "Simpan")
                }
            }
        }
    }
}
