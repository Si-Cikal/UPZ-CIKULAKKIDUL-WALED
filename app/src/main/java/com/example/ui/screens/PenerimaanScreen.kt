package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.JenisZakat
import com.example.data.model.MetodePembayaran
import com.example.data.model.StatusTransaksi
import com.example.data.model.WilayahCikulakKidul
import com.example.data.model.ZakatTransaction
import com.example.ui.components.CrispDrawableImage
import com.example.ui.components.EmptyStateCard
import com.example.ui.util.buildDigitalReceiptText
import com.example.ui.util.formatKg
import com.example.ui.util.formatRupiah
import com.example.ui.util.formatTimestamp
import com.example.ui.util.sharePlainText
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenerimaanScreen(
    transactionsState: UiState<List<ZakatTransaction>>,
    officerName: String,
    isSaving: Boolean,
    onAddZakatClick: () -> Unit,
    onEditTransaction: (ZakatTransaction) -> Unit,
    onVerifyTransaction: (String, String) -> Unit,
    onDeleteTransaction: (String, String) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedJenis by remember { mutableStateOf<String?>(null) }
    var selectedDusun by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedDetailTx by remember { mutableStateOf<ZakatTransaction?>(null) }
    var deleteCandidate by remember { mutableStateOf<ZakatTransaction?>(null) }

    val allItems = (transactionsState as? UiState.Success)?.data.orEmpty()
    val filteredItems = remember(allItems, searchQuery, selectedJenis, selectedDusun, selectedStatus) {
        allItems.filter { tx ->
            val matchesJenis = selectedJenis == null || tx.jenisZakat == selectedJenis
            val matchesStatus = selectedStatus == null || tx.status == selectedStatus
            val matchesDusun = selectedDusun == null || run {
                val d = selectedDusun!!
                val shortKey = d.substringAfter("(").substringBefore(")")
                tx.rtRw.contains(d, ignoreCase = true) ||
                    tx.rtRw.contains(shortKey, ignoreCase = true)
            }
            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                tx.namaMuzakki.lowercase().contains(q) ||
                tx.nomorKwitansi.lowercase().contains(q) ||
                tx.rtRw.lowercase().contains(q)
            matchesJenis && matchesStatus && matchesDusun && matchesSearch
        }
    }

    val filteredUang = filteredItems.sumOf { it.nominalUang }
    val filteredBeras = filteredItems.sumOf { it.beratBerasKg }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header + Search & Filter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Penerimaan Zakat & Infak",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Pencatatan Muzakki & Kwitansi Digital UPZ Desa Cikulak Kidul",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama muzakki, no. kwitansi, atau dusun...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
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
                    .testTag("search_muzakki_input"),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedJenis == null && selectedDusun == null && selectedStatus == null,
                    onClick = {
                        selectedJenis = null
                        selectedDusun = null
                        selectedStatus = null
                    },
                    label = { Text("Semua (${allItems.size})") },
                )
                StatusTransaksi.entries.forEach { st ->
                    FilterChip(
                        selected = selectedStatus == st.key,
                        onClick = {
                            selectedStatus = if (selectedStatus == st.key) null else st.key
                        },
                        label = { Text(st.label) },
                    )
                }
                JenisZakat.entries.forEach { jenis ->
                    FilterChip(
                        selected = selectedJenis == jenis.key,
                        onClick = {
                            selectedJenis = if (selectedJenis == jenis.key) null else jenis.key
                        },
                        label = { Text(jenis.shortLabel) },
                    )
                }
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

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${filteredItems.size} Transaksi",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "${formatRupiah(filteredUang)} • ${formatKg(filteredBeras)}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        when (transactionsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = transactionsState.message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            is UiState.Success -> {
                if (filteredItems.isEmpty()) {
                    val hasFilter = searchQuery.isNotBlank() || selectedJenis != null || selectedDusun != null || selectedStatus != null
                    Box(modifier = Modifier.padding(16.dp)) {
                        EmptyStateCard(
                            title = if (allItems.isEmpty()) "Belum Ada Data Penerimaan" else "Tidak Ada Hasil Pencarian",
                            description = if (allItems.isEmpty()) {
                                "Catat setoran Zakat Fitrah, Mal, Infak, atau Fidyah pertama dari warga Desa Cikulak Kidul."
                            } else {
                                "Coba ubah kata kunci pencarian atau pilih filter kategori lainnya."
                            },
                            ctaLabel = if (hasFilter && allItems.isNotEmpty()) "Reset Filter" else "Catat Penerimaan Baru",
                            onCtaClick = {
                                if (hasFilter && allItems.isNotEmpty()) {
                                    searchQuery = ""
                                    selectedJenis = null
                                    selectedDusun = null
                                    selectedStatus = null
                                } else {
                                    onAddZakatClick()
                                }
                            },
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(filteredItems, key = { it.id }) { tx ->
                            TransactionItemCard(
                                tx = tx,
                                onClick = { selectedDetailTx = tx },
                            )
                        }
                    }
                }
            }
        }
    }

    selectedDetailTx?.let { tx ->
        val isVerified = tx.status == StatusTransaksi.TERVERIFIKASI.key
        ModalBottomSheet(onDismissRequest = { selectedDetailTx = null }) {
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
                                text = "Kwitansi Digital UPZ",
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
                        color = if (isVerified) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = StatusTransaksi.fromKey(tx.status).label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isVerified) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
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
                        ReceiptDetailRow("Nomor Kwitansi", tx.nomorKwitansi)
                        ReceiptDetailRow("Nama Muzakki", tx.namaMuzakki)
                        ReceiptDetailRow("Alamat / Dusun", tx.rtRw)
                        ReceiptDetailRow("Jenis Zakat", JenisZakat.fromKey(tx.jenisZakat).label)
                        if (tx.jumlahJiwa > 0L) {
                            ReceiptDetailRow("Jumlah Jiwa", "${tx.jumlahJiwa} Jiwa")
                        }
                        if (tx.nominalUang > 0L) {
                            ReceiptDetailRow("Nominal Uang", formatRupiah(tx.nominalUang))
                        }
                        if (tx.beratBerasKg > 0.0) {
                            ReceiptDetailRow("Berat Beras", formatKg(tx.beratBerasKg))
                        }
                        ReceiptDetailRow("Metode", MetodePembayaran.fromKey(tx.metodePembayaran).label)
                        ReceiptDetailRow("Waktu Catat", formatTimestamp(tx.createdAt))
                        if (tx.catatan.isNotBlank()) {
                            ReceiptDetailRow("Catatan", tx.catatan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            sharePlainText(
                                context = context,
                                subject = "Kwitansi Zakat ${tx.nomorKwitansi}",
                                text = buildDigitalReceiptText(tx, officerName),
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_receipt_button"),
                    ) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan")
                    }
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(buildDigitalReceiptText(tx, officerName)))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_receipt_button"),
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin")
                    }
                }

                if (!isVerified) {
                    Button(
                        onClick = {
                            onVerifyTransaction(tx.id, tx.nomorKwitansi)
                            selectedDetailTx = null
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_tx_button"),
                    ) {
                        Icon(imageVector = Icons.Filled.Verified, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifikasi & Kunci Kwitansi")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (!isVerified) {
                        OutlinedButton(
                            onClick = {
                                selectedDetailTx = null
                                onEditTransaction(tx)
                            },
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ubah")
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            deleteCandidate = tx
                            selectedDetailTx = null
                        },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    deleteCandidate?.let { candidate ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Hapus Transaksi?") },
            text = {
                Text("Hapus kwitansi ${candidate.nomorKwitansi} atas nama ${candidate.namaMuzakki}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(candidate.id, candidate.namaMuzakki)
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
}

@Composable
private fun TransactionItemCard(
    tx: ZakatTransaction,
    onClick: () -> Unit,
) {
    val jenis = JenisZakat.fromKey(tx.jenisZakat)
    val isVerified = tx.status == StatusTransaksi.TERVERIFIKASI.key

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tx_card_${tx.id}"),
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
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.namaMuzakki,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${jenis.label} • ${MetodePembayaran.fromKey(tx.metodePembayaran).label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${tx.rtRw} • ${tx.nomorKwitansi}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (tx.nominalUang > 0L) {
                    Text(
                        text = formatRupiah(tx.nominalUang),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (tx.beratBerasKg > 0.0) {
                    Text(
                        text = formatKg(tx.beratBerasKg),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
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

@Composable
private fun ReceiptDetailRow(label: String, value: String) {
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
