package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.GolonganAsnaf
import com.example.data.model.MustahikRecord
import com.example.data.model.StatusPenyaluran
import com.example.data.model.WilayahCikulakKidul
import com.example.ui.components.CrispDrawableImage
import com.example.ui.components.EmptyStateCard
import com.example.ui.util.buildMustahikDistributionReceiptText
import com.example.ui.util.formatKg
import com.example.ui.util.formatRupiah
import com.example.ui.util.formatTimestamp
import com.example.ui.util.sharePlainText
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MustahikScreen(
    mustahikState: UiState<List<MustahikRecord>>,
    officerName: String,
    isSaving: Boolean,
    onAddMustahikClick: () -> Unit,
    onEditMustahik: (MustahikRecord) -> Unit,
    onDistributeMustahik: (String, String, Long, Double, String) -> Unit,
    onDeleteMustahik: (String, String) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedDusun by remember { mutableStateOf<String?>(null) }
    var selectedDetailMustahik by remember { mutableStateOf<MustahikRecord?>(null) }
    var distributeTarget by remember { mutableStateOf<MustahikRecord?>(null) }
    var deleteCandidate by remember { mutableStateOf<MustahikRecord?>(null) }

    val allList = (mustahikState as? UiState.Success)?.data.orEmpty()
    val filtered = remember(allList, searchQuery, selectedStatus, selectedDusun) {
        allList.filter { record ->
            val matchesStatus = selectedStatus == null || record.statusPenyaluran == selectedStatus
            val matchesDusun = selectedDusun == null || run {
                val d = selectedDusun!!
                val shortKey = d.substringAfter("(").substringBefore(")")
                record.rtRw.contains(d, ignoreCase = true) ||
                    record.rtRw.contains(shortKey, ignoreCase = true)
            }
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                record.namaMustahik.lowercase().contains(q) ||
                record.rtRw.lowercase().contains(q) ||
                GolonganAsnaf.fromKey(record.golonganAsnaf).label.lowercase().contains(q)
            matchesStatus && matchesDusun && matchesQuery
        }
    }

    val distributedCount = allList.count { it.statusPenyaluran == StatusPenyaluran.DISALURKAN.key }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Mustahik & Penyaluran",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Daftar 8 Golongan Asnaf penerima zakat Desa Cikulak Kidul",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama mustahik, asnaf, atau dusun...") },
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Bersihkan pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_mustahik_input"),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null },
                    label = { Text("Semua (${allList.size})") },
                )
                FilterChip(
                    selected = selectedStatus == StatusPenyaluran.TERDAFTAR.key,
                    onClick = {
                        selectedStatus = if (selectedStatus == StatusPenyaluran.TERDAFTAR.key) null else StatusPenyaluran.TERDAFTAR.key
                    },
                    label = { Text("Terdaftar (${allList.size - distributedCount})") },
                )
                FilterChip(
                    selected = selectedStatus == StatusPenyaluran.DISALURKAN.key,
                    onClick = {
                        selectedStatus = if (selectedStatus == StatusPenyaluran.DISALURKAN.key) null else StatusPenyaluran.DISALURKAN.key
                    },
                    label = { Text("Disalurkan ($distributedCount)") },
                )
                WilayahCikulakKidul.daftarDusun.forEach { dusun ->
                    val shortDusun = dusun.substringBefore(" (")
                    FilterChip(
                        selected = selectedDusun == dusun,
                        onClick = {
                            selectedDusun = if (selectedDusun == dusun) null else dusun
                        },
                        label = { Text(shortDusun) },
                    )
                }
            }
        }

        when (mustahikState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = mustahikState.message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            is UiState.Success -> {
                if (filtered.isEmpty()) {
                    val hasFilter = searchQuery.isNotBlank() || selectedStatus != null || selectedDusun != null
                    Box(modifier = Modifier.padding(16.dp)) {
                        EmptyStateCard(
                            title = if (allList.isEmpty()) "Belum Ada Data Mustahik" else "Data Tidak Ditemukan",
                            description = if (allList.isEmpty()) {
                                "Daftarkan warga penerima zakat (Fakir, Miskin, Amil, Fisabilillah, dll) untuk penyaluran yang tepat sasaran."
                            } else {
                                "Tidak ada data mustahik yang sesuai filter saat ini."
                            },
                            ctaLabel = if (hasFilter && allList.isNotEmpty()) "Reset Filter" else "Tambah Mustahik",
                            onCtaClick = {
                                if (hasFilter && allList.isNotEmpty()) {
                                    searchQuery = ""
                                    selectedStatus = null
                                    selectedDusun = null
                                } else {
                                    onAddMustahikClick()
                                }
                            },
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(filtered, key = { it.id }) { record ->
                            MustahikCard(
                                record = record,
                                isSaving = isSaving,
                                onCardClick = { selectedDetailMustahik = record },
                                onEditClick = { onEditMustahik(record) },
                                onDistributeClick = { distributeTarget = record },
                                onDeleteClick = { deleteCandidate = record },
                            )
                        }
                    }
                }
            }
        }
    }

    distributeTarget?.let { target ->
        var uangStr by remember(target.id) { mutableStateOf(target.bantuanUang.toString()) }
        var berasStr by remember(target.id) { mutableStateOf(target.bantuanBerasKg.toString()) }
        var catatanStr by remember(target.id) { mutableStateOf(target.catatan) }

        AlertDialog(
            onDismissRequest = { distributeTarget = null },
            title = { Text("Salurkan Bantuan Zakat") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Konfirmasi penyaluran untuk ${target.namaMustahik} (${GolonganAsnaf.fromKey(target.golonganAsnaf).label})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = uangStr,
                        onValueChange = { uangStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Santunan Uang (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = berasStr,
                        onValueChange = { berasStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Bantuan Beras (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = catatanStr,
                        onValueChange = { catatanStr = it },
                        label = { Text("Catatan Penyaluran") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDistributeMustahik(
                            target.id,
                            target.namaMustahik,
                            uangStr.toLongOrNull() ?: 0L,
                            berasStr.toDoubleOrNull() ?: 0.0,
                            catatanStr,
                        )
                        distributeTarget = null
                    },
                    enabled = !isSaving,
                ) {
                    Text("Salurkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { distributeTarget = null }) {
                    Text("Batal")
                }
            },
        )
    }

    deleteCandidate?.let { candidate ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Hapus Mustahik?") },
            text = { Text("Hapus data ${candidate.namaMustahik} dari daftar mustahik?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMustahik(candidate.id, candidate.namaMustahik)
                        deleteCandidate = null
                    },
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("Batal")
                }
            },
        )
    }
    selectedDetailMustahik?.let { record ->
        val asnaf = GolonganAsnaf.fromKey(record.golonganAsnaf)
        val isDistributed = record.statusPenyaluran == StatusPenyaluran.DISALURKAN.key
        ModalBottomSheet(onDismissRequest = { selectedDetailMustahik = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.size(48.dp),
                        ) {
                            CrispDrawableImage(
                                drawableRes = R.drawable.logo_upz_resmi,
                                contentDescription = "Logo UPZ",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "Tanda Terima Mustahik",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = WilayahCikulakKidul.NAMA_DESA_LENGKAP,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Surface(
                        color = if (isDistributed) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = StatusPenyaluran.fromKey(record.statusPenyaluran).label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDistributed) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }

                HorizontalDivider()

                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MustahikDetailRow("Nama Mustahik", record.namaMustahik)
                        MustahikDetailRow("Golongan Asnaf", asnaf.label)
                        MustahikDetailRow("Kriteria Asnaf", asnaf.deskripsi)
                        MustahikDetailRow("Alamat / Dusun", record.rtRw)
                        MustahikDetailRow("Santunan Uang", formatRupiah(record.bantuanUang))
                        MustahikDetailRow("Bantuan Beras", formatKg(record.bantuanBerasKg))
                        MustahikDetailRow("Diperbarui", formatTimestamp(record.updatedAt ?: record.createdAt))
                        if (record.catatan.isNotBlank()) {
                            MustahikDetailRow("Keterangan", record.catatan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            sharePlainText(
                                context = context,
                                subject = "Tanda Terima Penyaluran - ${record.namaMustahik}",
                                text = buildMustahikDistributionReceiptText(record, officerName),
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_mustahik_receipt_button"),
                    ) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan")
                    }
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(buildMustahikDistributionReceiptText(record, officerName)))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_mustahik_receipt_button"),
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin")
                    }
                }

                if (!isDistributed) {
                    Button(
                        onClick = {
                            selectedDetailMustahik = null
                            distributeTarget = record
                        },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salurkan Bantuan Sekarang")
                    }
                }
            }
        }
    }
}

@Composable
private fun MustahikDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MustahikCard(
    record: MustahikRecord,
    isSaving: Boolean,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDistributeClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val asnaf = GolonganAsnaf.fromKey(record.golonganAsnaf)
    val isDistributed = record.statusPenyaluran == StatusPenyaluran.DISALURKAN.key

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("mustahik_card_${record.id}"),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.VolunteerActivism,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.namaMustahik,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Asnaf ${asnaf.label} • ${record.rtRw}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isDistributed) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (isDistributed) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(13.dp),
                            )
                        }
                        Text(
                            text = StatusPenyaluran.fromKey(record.statusPenyaluran).label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDistributed) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildString {
                            append(formatRupiah(record.bantuanUang))
                            if (record.bantuanBerasKg > 0.0) {
                                append(" + ${formatKg(record.bantuanBerasKg)}")
                            }
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (record.catatan.isNotBlank()) {
                        Text(
                            text = record.catatan,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isDistributed) {
                        IconButton(
                            onClick = onEditClick,
                            enabled = !isSaving,
                            modifier = Modifier.testTag("edit_mustahik_button_${record.id}"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Ubah data mustahik",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Button(
                            onClick = onDistributeClick,
                            enabled = !isSaving,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("distribute_button_${record.id}"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salurkan")
                        }
                    }
                    IconButton(onClick = onDeleteClick, enabled = !isSaving) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Hapus mustahik",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
