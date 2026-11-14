package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AmilProfile
import com.example.data.model.GolonganAsnaf
import com.example.data.model.JenisZakat
import com.example.data.model.StatusTransaksi
import com.example.data.model.WilayahCikulakKidul
import com.example.data.model.ZakatTransaction
import com.example.ui.components.CrispDrawableImage
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.VillageProgressRow
import com.example.ui.components.ZisBreakdownChartCard
import com.example.ui.util.formatDateShort
import com.example.ui.util.formatKg
import com.example.ui.util.formatRupiah
import com.example.ui.util.formatRupiahCompact
import com.example.ui.viewmodel.DashboardSummary
import com.example.ui.viewmodel.UiState

@Composable
fun DashboardScreen(
    officerPhotoUrl: String?,
    officerEmail: String?,
    profileState: UiState<AmilProfile?>,
    transactionsState: UiState<List<ZakatTransaction>>,
    summary: DashboardSummary,
    onOpenAddZakat: () -> Unit,
    onOpenAddMustahik: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenLaporan: () -> Unit,
    onOpenProfileSettings: () -> Unit,
    onViewAllPenerimaan: () -> Unit,
    onViewAllMustahik: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    val profile = (profileState as? UiState.Success)?.data
    val transactions = (transactionsState as? UiState.Success)?.data.orEmpty()
    val recentTransactions = transactions.take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Top Officer & Village Header
        item {
            OfficerTopHeader(
                profile = profile,
                officerPhotoUrl = officerPhotoUrl,
                officerEmail = officerEmail,
                onOpenSettings = onOpenProfileSettings,
                onSignOutClick = onSignOutClick,
            )
        }

        // 2. Hero Village Summary Card
        item {
            VillageHeroSummaryCard(
                summary = summary,
                profile = profile,
            )
        }

        // 3. Quick Action Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickActionTile(
                    label = "Catat Zakat",
                    icon = Icons.Filled.AddCircle,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = onOpenAddZakat,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_zakat"),
                )
                QuickActionTile(
                    label = "Mustahik",
                    icon = Icons.Filled.PersonAdd,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onOpenAddMustahik,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_mustahik"),
                )
                QuickActionTile(
                    label = "Kalkulator",
                    icon = Icons.Filled.Calculate,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = onOpenCalculator,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_calculator"),
                )
                QuickActionTile(
                    label = "Laporan",
                    icon = Icons.Filled.Assessment,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onOpenLaporan,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_laporan"),
                )
            }
        }

        // 4. 2x2 KPI Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KpiMetricCard(
                        title = "Zakat Fitrah",
                        value = formatKg(summary.totalBerasTerkumpulKg),
                        subtitle = "${formatRupiahCompact(summary.totalFitrahUang)} • ${summary.totalJiwaFitrah} jiwa",
                        icon = Icons.Filled.Grain,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    KpiMetricCard(
                        title = "Zakat Mal",
                        value = formatRupiahCompact(summary.totalMalUang),
                        subtitle = "Harta & Penghasilan",
                        icon = Icons.Filled.AccountBalanceWallet,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KpiMetricCard(
                        title = "Infak & Fidyah",
                        value = formatRupiahCompact(summary.totalInfakFidyahUang),
                        subtitle = "${summary.totalTerverifikasi} kwitansi terverifikasi",
                        icon = Icons.Filled.VolunteerActivism,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    KpiMetricCard(
                        title = "Penyaluran",
                        value = "${summary.rasioPenyaluranPersen}%",
                        subtitle = "${summary.totalMustahikDisalurkan}/${summary.totalMustahikCount} mustahik",
                        icon = Icons.Filled.Groups,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // 5. ZIS Composition Donut Chart
        item {
            ZisBreakdownChartCard(
                fitrahUang = summary.totalFitrahUang,
                malUang = summary.totalMalUang,
                infakFidyahUang = summary.totalInfakFidyahUang,
            )
        }

        // 6. Sebaran 8 Golongan Asnaf Card
        item {
            AsnafSummaryCard(
                asnafCounts = summary.asnafCounts,
                totalMustahik = summary.totalMustahikCount,
                onViewAll = onViewAllMustahik,
            )
        }

        // 7. Aktivitas Pencatatan Terbaru
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Transaksi Muzakki Terbaru",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Desa Cikulak Kidul, Kec. Waled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (transactions.isNotEmpty()) {
                    TextButton(onClick = onViewAllPenerimaan) {
                        Text("Lihat Semua")
                    }
                }
            }
        }

        when (transactionsState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is UiState.Error -> {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = transactionsState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            is UiState.Success -> {
                if (recentTransactions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Belum Ada Penerimaan Zakat",
                            description = "Mulai catat penerimaan Zakat Fitrah, Zakat Mal, Infak, atau Fidyah warga Desa Cikulak Kidul.",
                            ctaLabel = "Catat Zakat Pertama",
                            onCtaClick = onOpenAddZakat,
                        )
                    }
                } else {
                    items(recentTransactions, key = { it.id }) { tx ->
                        RecentTransactionRow(
                            tx = tx,
                            onClick = onViewAllPenerimaan,
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun OfficerTopHeader(
    profile: AmilProfile?,
    officerPhotoUrl: String?,
    officerEmail: String?,
    onOpenSettings: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    val officerName = profile?.namaPetugas?.ifBlank { "Petugas Amil UPZ" } ?: "Petugas Amil UPZ"
    val roleAndDusun = if (profile != null) {
        "${profile.jabatan} • ${profile.dusun}"
    } else {
        WilayahCikulakKidul.NAMA_UPZ
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!officerPhotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = officerPhotoUrl,
                    contentDescription = "Foto petugas $officerName",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                )
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(46.dp)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = officerName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = officerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = roleAndDusun,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = officerEmail ?: WilayahCikulakKidul.NAMA_DESA_LENGKAP,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("header_settings_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Pengaturan Profil & Target UPZ",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onSignOutClick,
                modifier = Modifier.testTag("header_logout_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Keluar Akun",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun VillageHeroSummaryCard(
    summary: DashboardSummary,
    profile: AmilProfile?,
) {
    val targetUang = profile?.targetZakatUang?.takeIf { it > 0L } ?: 50_000_000L
    val targetBeras = profile?.targetFitrahBerasKg?.takeIf { it > 0.0 } ?: 2500.0
    val progressUang = (summary.totalUangTerkumpul.toFloat() / targetUang.toFloat())
    val progressBeras = (summary.totalBerasTerkumpulKg.toFloat() / targetBeras.toFloat())

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFF5B731).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            CrispDrawableImage(
                drawableRes = R.drawable.img_dashboard_banner,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF063427).copy(alpha = 0.74f),
                                Color(0xFF0A4836).copy(alpha = 0.58f),
                            )
                        )
                    )
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFF5B731).copy(alpha = 0.65f)),
                            modifier = Modifier.size(42.dp),
                        ) {
                            CrispDrawableImage(
                                drawableRes = R.drawable.logo_upz_resmi,
                                contentDescription = "Logo Resmi UPZ",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "UPZ DESA CIKULAK KIDUL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFFEFC7),
                            )
                            Text(
                                text = "Kec. Waled, Kab. Cirebon",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                            )
                        }
                    }
                    Surface(
                        color = Color(0xFF04241B).copy(alpha = 0.55f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFFF5B731).copy(alpha = 0.45f)),
                    ) {
                        Text(
                            text = "AMANAH",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF7CB67),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Total Dana ZIS Terhimpun",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.88f),
                )
                Text(
                    text = formatRupiah(summary.totalUangTerkumpul),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.32f),
                            offset = Offset(0f, 2f),
                            blurRadius = 4f,
                        ),
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Beras Fitrah: ${formatKg(summary.totalBerasTerkumpulKg)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFF5B731),
                    )
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.5f),
                    )
                    Text(
                        text = "${summary.totalMuzakkiCount} Muzakki",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.92f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                VillageProgressRow(
                    label = "Target Dana Desa",
                    currentLabel = formatRupiahCompact(summary.totalUangTerkumpul),
                    targetLabel = formatRupiahCompact(targetUang),
                    progress = progressUang,
                    barColor = Color(0xFFF5B731),
                    trackColor = Color.White.copy(alpha = 0.2f),
                    labelColor = Color.White,
                )

                Spacer(modifier = Modifier.height(10.dp))

                VillageProgressRow(
                    label = "Target Beras Fitrah",
                    currentLabel = formatKg(summary.totalBerasTerkumpulKg),
                    targetLabel = formatKg(targetBeras),
                    progress = progressBeras,
                    barColor = Color(0xFF6ADBB5),
                    trackColor = Color.White.copy(alpha = 0.2f),
                    labelColor = Color.White,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HeroMiniStat(
                        label = "Saldo Kas ZIS",
                        value = formatRupiahCompact(summary.sisaSaldoUang),
                        modifier = Modifier.weight(1f),
                    )
                    HeroMiniStat(
                        label = "Stok Beras",
                        value = formatKg(summary.sisaStokBerasKg),
                        modifier = Modifier.weight(1f),
                    )
                    HeroMiniStat(
                        label = "Dana Tersalur",
                        value = formatRupiahCompact(summary.totalUangDisalurkan),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.13f),
        shape = MaterialTheme.shapes.small,
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = CircleShape,
                color = containerColor,
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AsnafSummaryCard(
    asnafCounts: Map<GolonganAsnaf, Int>,
    totalMustahik: Int,
    onViewAll: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Penerima Manfaat (8 Asnaf)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Total $totalMustahik mustahik terdaftar di Desa Cikulak Kidul",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onViewAll) {
                    Text("Kelola")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GolonganAsnaf.entries.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowItems.forEach { asnaf ->
                            val count = asnafCounts[asnaf] ?: 0
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = asnaf.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionRow(
    tx: ZakatTransaction,
    onClick: () -> Unit,
) {
    val jenis = JenisZakat.fromKey(tx.jenisZakat)
    val isVerified = tx.status == StatusTransaksi.TERVERIFIKASI.key

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.namaMuzakki,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${jenis.shortLabel} • ${tx.rtRw}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${tx.nomorKwitansi} • ${formatDateShort(tx.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                val amountLabel = when {
                    tx.nominalUang > 0L && tx.beratBerasKg > 0.0 ->
                        "${formatRupiahCompact(tx.nominalUang)} + ${formatKg(tx.beratBerasKg)}"
                    tx.beratBerasKg > 0.0 -> formatKg(tx.beratBerasKg)
                    else -> formatRupiah(tx.nominalUang)
                }
                Text(
                    text = amountLabel,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isVerified) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        if (isVerified) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                        Text(
                            text = StatusTransaksi.fromKey(tx.status).label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isVerified) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                        )
                    }
                }
            }
        }
    }
}
