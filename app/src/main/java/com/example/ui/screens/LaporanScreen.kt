package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AmilProfile
import com.example.data.model.GolonganAsnaf
import com.example.data.model.MustahikRecord
import com.example.data.model.StatusPenyaluran
import com.example.data.model.StatusTransaksi
import com.example.data.model.WilayahCikulakKidul
import com.example.data.model.ZakatTransaction
import com.example.ui.components.CrispDrawableImage
import com.example.ui.util.buildCsvReportText
import com.example.ui.util.buildVillageReportText
import com.example.ui.util.formatKg
import com.example.ui.util.formatRupiah
import com.example.ui.util.formatRupiahCompact
import com.example.ui.util.sharePlainText
import com.example.ui.viewmodel.DashboardSummary
import com.example.ui.viewmodel.UiState

private data class DusunStat(
    val dusunName: String,
    val txCount: Int,
    val totalUang: Long,
    val totalBerasKg: Double,
    val mustahikCount: Int,
)

@Composable
fun LaporanScreen(
    profileState: UiState<AmilProfile?>,
    transactionsState: UiState<List<ZakatTransaction>>,
    mustahikState: UiState<List<MustahikRecord>>,
    summary: DashboardSummary,
    onShowMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val profile = (profileState as? UiState.Success)?.data
    val transactions = (transactionsState as? UiState.Success)?.data.orEmpty()
    val mustahikList = (mustahikState as? UiState.Success)?.data.orEmpty()

    val dusunStats = remember(transactions, mustahikList) {
        WilayahCikulakKidul.daftarDusun.map { dusun ->
            val shortKey = dusun.substringAfter("(").substringBefore(")").lowercase()
            val matchedTx = transactions.filter {
                it.rtRw.contains(dusun, ignoreCase = true) ||
                    it.rtRw.contains(shortKey, ignoreCase = true)
            }
            val matchedMustahik = mustahikList.filter {
                it.rtRw.contains(dusun, ignoreCase = true) ||
                    it.rtRw.contains(shortKey, ignoreCase = true)
            }
            DusunStat(
                dusunName = dusun,
                txCount = matchedTx.size,
                totalUang = matchedTx.sumOf { it.nominalUang },
                totalBerasKg = matchedTx.sumOf { it.beratBerasKg },
                mustahikCount = matchedMustahik.size,
            )
        }
    }

    val maxDusunUang = dusunStats.maxOfOrNull { it.totalUang }?.coerceAtLeast(1L) ?: 1L
    val verifiedCount = transactions.count { it.status == StatusTransaksi.TERVERIFIKASI.key }
    val unverifiedCount = transactions.size - verifiedCount

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
                    text = "Laporan & Rekapitulasi Desa",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Akuntabilitas Penghimpunan & Penyaluran ${WilayahCikulakKidul.NAMA_UPZ}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Official UPZ Letterhead Summary Card
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)),
                            modifier = Modifier.size(62.dp),
                        ) {
                            CrispDrawableImage(
                                drawableRes = R.drawable.logo_upz_resmi,
                                contentDescription = "Logo Resmi UPZ",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "UNIT PENGUMPUL ZAKAT (UPZ)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Text(
                                text = "Desa Cikulak Kidul",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = WilayahCikulakKidul.KODE_WILAYAH,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    ReportBalanceRow("Total Dana Masuk", formatRupiah(summary.totalUangTerkumpul), MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportBalanceRow("Dana Disalurkan", formatRupiah(summary.totalUangDisalurkan), MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportBalanceRow("Saldo Kas ZIS Tersedia", formatRupiah(summary.sisaSaldoUang), MaterialTheme.colorScheme.onSurface)

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    ReportBalanceRow("Beras Fitrah Masuk", formatKg(summary.totalBerasTerkumpulKg), MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportBalanceRow("Beras Disalurkan", formatKg(summary.totalBerasDisalurkanKg), MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportBalanceRow("Stok Beras Tersedia", formatKg(summary.sisaStokBerasKg), MaterialTheme.colorScheme.onSurface)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = {
                                val reportText = buildVillageReportText(profile, summary, transactions, mustahikList)
                                sharePlainText(
                                    context = context,
                                    subject = "Laporan UPZ Desa Cikulak Kidul",
                                    text = reportText,
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_village_report_button"),
                        ) {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bagikan Laporan")
                        }
                        OutlinedButton(
                            onClick = {
                                val reportText = buildVillageReportText(profile, summary, transactions, mustahikList)
                                clipboard.setText(AnnotatedString(reportText))
                                onShowMessage("Ringkasan laporan UPZ disalin ke clipboard.")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_village_report_button"),
                        ) {
                            Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salin Teks")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val csvText = buildCsvReportText(transactions, mustahikList)
                            sharePlainText(
                                context = context,
                                subject = "Data CSV UPZ Desa Cikulak Kidul",
                                text = csvText,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_csv_report_button"),
                    ) {
                        Icon(imageVector = Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ekspor Data CSV (Excel / Spreadsheet)")
                    }
                }
            }
        }

        // Verification Status Card
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audit Verifikasi Kwitansi",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "$verifiedCount kwitansi terverifikasi • $unverifiedCount menunggu verifikasi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Rekapitulasi Per Dusun (Dusun I - V Desa Cikulak Kidul)
        item {
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
                            imageVector = Icons.Filled.LocationCity,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column {
                            Text(
                                text = "Rekapitulasi Per Dusun",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Cakupan wilayah Dusun I – V Desa Cikulak Kidul",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    HorizontalDivider()

                    dusunStats.forEach { stat ->
                        val ratio = (stat.totalUang.toFloat() / maxDusunUang.toFloat()).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stat.dusunName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "${formatRupiahCompact(stat.totalUang)} • ${formatKg(stat.totalBerasKg)}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            LinearProgressIndicator(
                                progress = { ratio },
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(CircleShape),
                            )
                            Text(
                                text = "${stat.txCount} setoran muzakki • ${stat.mustahikCount} mustahik terdaftar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Rekapitulasi Penyaluran per 8 Golongan Asnaf
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
                    Text(
                        text = "Realisasi Penyaluran 8 Asnaf",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    HorizontalDivider()
                    GolonganAsnaf.entries.forEach { asnaf ->
                        val group = mustahikList.filter { it.golonganAsnaf == asnaf.key }
                        val sent = group.filter { it.statusPenyaluran == StatusPenyaluran.DISALURKAN.key }
                        val uangSent = sent.sumOf { it.bantuanUang }
                        val berasSent = sent.sumOf { it.bantuanBerasKg }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = asnaf.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "${sent.size}/${group.size} penerima disalurkan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = if (uangSent > 0L || berasSent > 0.0) {
                                    "${formatRupiahCompact(uangSent)} • ${formatKg(berasSent)}"
                                } else {
                                    "-"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun ReportBalanceRow(
    label: String,
    value: String,
    valueColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
        )
    }
}
