package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AmilProfile
import com.example.data.model.GolonganAsnaf
import com.example.data.model.JenisZakat
import com.example.data.model.WilayahCikulakKidul
import com.example.ui.util.formatKg
import com.example.ui.util.formatRupiah
import com.example.ui.viewmodel.ZakatPrefill

@Composable
fun KalkulatorScreen(
    profile: AmilProfile?,
    onRecordCalculatedZakat: (ZakatPrefill) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Fitrah", "Zakat Mal", "Profesi", "Pertanian", "Fidyah")

    val defaultHargaBeras = profile?.hargaBerasPerKg ?: 14_000L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Kalkulator & Panduan Zakat",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Acuan standar ${WilayahCikulakKidul.NAMA_UPZ}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tabs.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(label) },
                        modifier = Modifier.testTag("calc_tab_$index"),
                    )
                }
            }
        }

        item {
            when (selectedTab) {
                0 -> FitrahCalculatorCard(
                    defaultHargaBeras = defaultHargaBeras,
                    onRecord = onRecordCalculatedZakat,
                )
                1 -> MalCalculatorCard(onRecord = onRecordCalculatedZakat)
                2 -> ProfesiCalculatorCard(onRecord = onRecordCalculatedZakat)
                3 -> PertanianCalculatorCard(
                    defaultHargaBeras = defaultHargaBeras,
                    onRecord = onRecordCalculatedZakat,
                )
                else -> FidyahCalculatorCard(
                    defaultHargaBeras = defaultHargaBeras,
                    onRecord = onRecordCalculatedZakat,
                )
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Referensi 8 Golongan Asnaf",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    HorizontalDivider()
                    GolonganAsnaf.entries.forEach { asnaf ->
                        Column {
                            Text(
                                text = asnaf.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = asnaf.deskripsi,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        item {
            NiatDanDoaZakatCard()
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun NiatDanDoaZakatCard() {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = "Panduan Niat & Doa Serah Terima Zakat",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            HorizontalDivider()

            DoaSectionBlock(
                judul = "Niat Zakat Fitrah (Untuk Diri Sendiri & Keluarga)",
                latin = "Nawaitu an ukhrija zakaatal fithri 'anni wa 'an jami'i ma yalzamuni nafaqatuhum syar'an fardhan lillahi ta'ala.",
                arti = "Aku niat mengeluarkan zakat fitrah untuk diriku dan seluruh orang yang nafkahnya menjadi tanggunganku fardu karena Allah Ta'ala.",
            )
            HorizontalDivider()
            DoaSectionBlock(
                judul = "Niat Zakat Mal (Harta)",
                latin = "Nawaitu an ukhrija zakaata maali fardhan lillahi ta'ala.",
                arti = "Aku niat mengeluarkan zakat hartaku fardu karena Allah Ta'ala.",
            )
            HorizontalDivider()
            DoaSectionBlock(
                judul = "Doa Amil Saat Menerima Zakat dari Muzakki",
                latin = "Ajarakallahu fima a'thaita wa baraka fima abqaita wa ja'alahu laka thahuran.",
                arti = "Semoga Allah memberikan pahala kepadamu atas apa yang engkau berikan, memberkahi harta yang tersisa, dan menjadikannya penyuci bagimu.",
            )
        }
    }
}

@Composable
private fun DoaSectionBlock(
    judul: String,
    latin: String,
    arti: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = judul,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = latin,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Artinya: \"$arti\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FitrahCalculatorCard(
    defaultHargaBeras: Long,
    onRecord: (ZakatPrefill) -> Unit,
) {
    var jiwa by remember { mutableIntStateOf(4) }
    var hargaBerasText by remember(defaultHargaBeras) {
        mutableStateOf(defaultHargaBeras.toString())
    }
    val hargaBeras = hargaBerasText.toLongOrNull() ?: defaultHargaBeras
    val totalBerasKg = jiwa * 2.5
    val totalUang = (totalBerasKg * hargaBeras).toLong()

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Hitung Zakat Fitrah (2,5 kg / Jiwa)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Jumlah Jiwa Keluarga",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FilledTonalIconButton(
                        onClick = { if (jiwa > 1) jiwa-- },
                    ) {
                        Text("-", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "$jiwa Jiwa",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    FilledTonalIconButton(
                        onClick = { if (jiwa < 50) jiwa++ },
                    ) {
                        Text("+", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            OutlinedTextField(
                value = hargaBerasText,
                onValueChange = { hargaBerasText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Harga Beras Lokal per kg (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kewajiban Zakat Fitrah",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${formatKg(totalBerasKg)} Beras  atau  ${formatRupiah(totalUang)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = {
                        onRecord(
                            ZakatPrefill(
                                jenisZakat = JenisZakat.FITRAH_UANG.key,
                                jumlahJiwa = jiwa.toLong(),
                                nominalUang = totalUang,
                                beratBerasKg = 0.0,
                                catatan = "Konversi $jiwa jiwa x 2,5 kg (${formatRupiah(hargaBeras)}/kg)",
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("calc_record_fitrah_uang"),
                ) {
                    Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Catat Uang")
                }
                Button(
                    onClick = {
                        onRecord(
                            ZakatPrefill(
                                jenisZakat = JenisZakat.FITRAH_BERAS.key,
                                jumlahJiwa = jiwa.toLong(),
                                nominalUang = 0L,
                                beratBerasKg = totalBerasKg,
                                catatan = "Zakat fitrah beras $jiwa jiwa",
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("calc_record_fitrah_beras"),
                ) {
                    Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Catat Beras")
                }
            }
        }
    }
}

@Composable
private fun MalCalculatorCard(
    onRecord: (ZakatPrefill) -> Unit,
) {
    var totalHartaText by remember { mutableStateOf("150000000") }
    var hutangText by remember { mutableStateOf("0") }
    var hargaEmasText by remember { mutableStateOf("1500000") }

    val harta = totalHartaText.toLongOrNull() ?: 0L
    val hutang = hutangText.toLongOrNull() ?: 0L
    val hargaEmas = hargaEmasText.toLongOrNull() ?: 1_500_000L

    val hartaBersih = (harta - hutang).coerceAtLeast(0L)
    val nisab = 85L * hargaEmas
    val wajibZakat = hartaBersih >= nisab && hartaBersih > 0L
    val zakatMal = if (wajibZakat) (hartaBersih * 25L) / 1000L else 0L

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Hitung Zakat Mal (2,5% • Nisab 85 gr Emas)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            OutlinedTextField(
                value = totalHartaText,
                onValueChange = { totalHartaText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Total Harta / Tabungan / Hasil Panen (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = hutangText,
                onValueChange = { hutangText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Kewajiban / Utang Jatuh Tempo (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = hargaEmasText,
                onValueChange = { hargaEmasText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Harga Emas per Gram (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Surface(
                color = if (wajibZakat) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nisab 85 gr Emas: ${formatRupiah(nisab)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (wajibZakat) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (wajibZakat) {
                            "Wajib Zakat Mal: ${formatRupiah(zakatMal)}"
                        } else {
                            "Belum Mencapai Nisab (Dapat dicatat sebagai Infak/Sedekah)"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (wajibZakat) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                }
            }

            if (wajibZakat) {
                Button(
                    onClick = {
                        onRecord(
                            ZakatPrefill(
                                jenisZakat = JenisZakat.MAL.key,
                                jumlahJiwa = 0L,
                                nominalUang = zakatMal,
                                beratBerasKg = 0.0,
                                catatan = "Zakat Mal 2,5% dari harta bersih ${formatRupiah(hartaBersih)}",
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Catat Zakat Mal ke Penerimaan")
                }
            }
        }
    }
}

@Composable
private fun FidyahCalculatorCard(
    defaultHargaBeras: Long,
    onRecord: (ZakatPrefill) -> Unit,
) {
    var hari by remember { mutableIntStateOf(7) }
    var tarifPerHariText by remember(defaultHargaBeras) {
        mutableStateOf(((defaultHargaBeras * 75L) / 100L).coerceAtLeast(15_000L).toString())
    }
    val tarif = tarifPerHariText.toLongOrNull() ?: 15_000L
    val totalFidyah = hari * tarif
    val totalBerasKg = hari * 0.75

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Hitung Fidyah (1 Mud / ~0,75 kg per Hari)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Jumlah Hari Puasa Tertinggal", style = MaterialTheme.typography.bodyMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FilledTonalIconButton(onClick = { if (hari > 1) hari-- }) {
                        Text("-", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "$hari Hari",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    FilledTonalIconButton(onClick = { if (hari < 90) hari++ }) {
                        Text("+", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            OutlinedTextField(
                value = tarifPerHariText,
                onValueChange = { tarifPerHariText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Estimasi Nilai 1 Porsi / Hari (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Fidyah ($hari Hari)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formatRupiah(totalFidyah)}  atau  ${formatKg(totalBerasKg)} Beras",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            Button(
                onClick = {
                    onRecord(
                        ZakatPrefill(
                            jenisZakat = JenisZakat.FIDYAH.key,
                            jumlahJiwa = hari.toLong(),
                            nominalUang = totalFidyah,
                            beratBerasKg = 0.0,
                            catatan = "Fidyah $hari hari",
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Catat Fidyah ke Penerimaan")
            }
        }
    }
}

@Composable
private fun ProfesiCalculatorCard(
    onRecord: (ZakatPrefill) -> Unit,
) {
    var penghasilanBulanText by remember { mutableStateOf("9000000") }
    var bonusText by remember { mutableStateOf("0") }
    var hargaEmasText by remember { mutableStateOf("1500000") }

    val gaji = penghasilanBulanText.toLongOrNull() ?: 0L
    val bonus = bonusText.toLongOrNull() ?: 0L
    val hargaEmas = hargaEmasText.toLongOrNull() ?: 1_500_000L

    val totalPenghasilan = gaji + bonus
    val nisabBulanan = (85L * hargaEmas) / 12L
    val wajibZakat = totalPenghasilan >= nisabBulanan && totalPenghasilan > 0L
    val zakatProfesi = if (wajibZakat) (totalPenghasilan * 25L) / 1000L else 0L

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Zakat Penghasilan / Profesi (2,5% per Bulan)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            OutlinedTextField(
                value = penghasilanBulanText,
                onValueChange = { penghasilanBulanText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Penghasilan / Gaji Bulanan (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = bonusText,
                onValueChange = { bonusText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Tunjangan / Pendapatan Lainnya (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = hargaEmasText,
                onValueChange = { hargaEmasText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Acuan Harga Emas per Gram (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Surface(
                color = if (wajibZakat) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nisab Bulanan (85 gr Emas ÷ 12): ${formatRupiah(nisabBulanan)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (wajibZakat) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (wajibZakat) {
                            "Zakat Profesi: ${formatRupiah(zakatProfesi)} / bulan"
                        } else {
                            "Belum Mencapai Nisab (Dapat dicatat sebagai Infak)"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (wajibZakat) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                }
            }

            if (wajibZakat) {
                Button(
                    onClick = {
                        onRecord(
                            ZakatPrefill(
                                jenisZakat = JenisZakat.MAL.key,
                                jumlahJiwa = 0L,
                                nominalUang = zakatProfesi,
                                beratBerasKg = 0.0,
                                catatan = "Zakat Profesi 2,5% dari penghasilan ${formatRupiah(totalPenghasilan)}",
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Catat Zakat Profesi ke Penerimaan")
                }
            }
        }
    }
}

@Composable
private fun PertanianCalculatorCard(
    defaultHargaBeras: Long,
    onRecord: (ZakatPrefill) -> Unit,
) {
    var hasilPanenKgText by remember { mutableStateOf("1200") }
    var beririgasiBuatan by remember { mutableStateOf(true) }

    val hasilKg = hasilPanenKgText.toDoubleOrNull() ?: 0.0
    val nisabGabahKg = 653.0
    val tarifPersen = if (beririgasiBuatan) 0.05 else 0.10
    val wajibZakat = hasilKg >= nisabGabahKg
    val zakatKg = if (wajibZakat) hasilKg * tarifPersen else 0.0
    val estimasiUang = (zakatKg * defaultHargaBeras).toLong()

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Zakat Pertanian / Panen (Nisab 653 kg Gabah)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            OutlinedTextField(
                value = hasilPanenKgText,
                onValueChange = { hasilPanenKgText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Total Hasil Panen Bersih (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Sumber Pengairan Sawah", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = beririgasiBuatan,
                    onClick = { beririgasiBuatan = true },
                    label = { Text("Irigasi / Pompa (5%)") },
                )
                FilterChip(
                    selected = !beririgasiBuatan,
                    onClick = { beririgasiBuatan = false },
                    label = { Text("Tadah Hujan (10%)") },
                )
            }

            Surface(
                color = if (wajibZakat) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (wajibZakat) {
                            "Mencapai Nisab (≥ 653 kg) • Tarif ${if (beririgasiBuatan) "5%" else "10%"}"
                        } else {
                            "Belum Mencapai Nisab (Minimal 653 kg)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (wajibZakat) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (wajibZakat) {
                            "Zakat Panen: ${formatKg(zakatKg)} (≈ ${formatRupiah(estimasiUang)})"
                        } else {
                            "Dapat disalurkan sebagai Infak Hasil Bumi"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (wajibZakat) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                }
            }

            if (wajibZakat) {
                Button(
                    onClick = {
                        onRecord(
                            ZakatPrefill(
                                jenisZakat = JenisZakat.MAL.key,
                                jumlahJiwa = 0L,
                                nominalUang = estimasiUang,
                                beratBerasKg = 0.0,
                                catatan = "Zakat Pertanian ${if (beririgasiBuatan) "5%" else "10%"} dari panen ${formatKg(hasilKg)}",
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Catat Zakat Pertanian ke Penerimaan")
                }
            }
        }
    }
}
