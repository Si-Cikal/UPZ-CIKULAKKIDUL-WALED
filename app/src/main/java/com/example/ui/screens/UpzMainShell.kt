package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.auth.signOutUser
import com.example.data.model.MustahikRecord
import com.example.data.model.ZakatTransaction
import com.example.data.repository.UpzRepository
import com.example.ui.components.CrispDrawableImage
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.UpzDashboardViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_PENERIMAAN = "penerimaan"
const val ROUTE_MUSTAHIK = "mustahik"
const val ROUTE_LAPORAN = "laporan"
const val ROUTE_KALKULATOR = "kalkulator"

private data class UpzNavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val upzDestinations = listOf(
    UpzNavDestination(ROUTE_DASHBOARD, "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    UpzNavDestination(ROUTE_PENERIMAAN, "Zakat", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
    UpzNavDestination(ROUTE_MUSTAHIK, "Mustahik", Icons.Filled.People, Icons.Outlined.People),
    UpzNavDestination(ROUTE_LAPORAN, "Laporan", Icons.Filled.Assessment, Icons.Outlined.Assessment),
    UpzNavDestination(ROUTE_KALKULATOR, "Kalkulator", Icons.Filled.Calculate, Icons.Outlined.Calculate),
)

@Composable
fun UpzMainShell(
    currentUserId: String,
    officerDisplayName: String?,
    officerEmail: String?,
    officerPhotoUrl: String?,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: UpzDashboardViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                UpzDashboardViewModel(
                    repository = UpzRepository(db),
                    currentUserId = currentUserId,
                    displayName = officerDisplayName,
                )
            }
        },
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val transactionsState by viewModel.transactionsState.collectAsStateWithLifecycle()
    val mustahikState by viewModel.mustahikState.collectAsStateWithLifecycle()
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val zakatPrefill by viewModel.zakatPrefill.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_DASHBOARD

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionMessage) {
        val msg = actionMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearMessage()
    }

    var showAddZakatSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<ZakatTransaction?>(null) }
    var showAddMustahikSheet by remember { mutableStateOf(false) }
    var editingMustahik by remember { mutableStateOf<MustahikRecord?>(null) }
    var showProfileSheet by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val activeProfile = (profileState as? UiState.Success)?.data
    val hargaBerasPerKg = activeProfile?.hargaBerasPerKg ?: 14_000L
    val activeOfficerName = activeProfile?.namaPetugas?.ifBlank { null }
        ?: officerDisplayName
        ?: "Petugas Amil UPZ"

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar {
                        upzDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label,
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("nav_${dest.route}"),
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (currentRoute == ROUTE_DASHBOARD || currentRoute == ROUTE_PENERIMAAN) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            viewModel.setZakatPrefill(null)
                            editingTransaction = null
                            showAddZakatSheet = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                        text = { Text("Catat Zakat") },
                        modifier = Modifier.testTag("fab_add_zakat"),
                    )
                } else if (currentRoute == ROUTE_MUSTAHIK) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            editingMustahik = null
                            showAddMustahikSheet = true
                        },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                        text = { Text("Tambah Mustahik") },
                        modifier = Modifier.testTag("fab_add_mustahik"),
                    )
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .size(46.dp),
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
                        },
                    ) {
                        upzDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label,
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("rail_${dest.route}"),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_DASHBOARD,
                    modifier = Modifier.weight(1f),
                ) {
                    composable(ROUTE_DASHBOARD) {
                        DashboardScreen(
                            officerPhotoUrl = officerPhotoUrl,
                            officerEmail = officerEmail,
                            profileState = profileState,
                            transactionsState = transactionsState,
                            summary = summary,
                            onOpenAddZakat = {
                                viewModel.setZakatPrefill(null)
                                editingTransaction = null
                                showAddZakatSheet = true
                            },
                            onOpenAddMustahik = {
                                editingMustahik = null
                                showAddMustahikSheet = true
                            },
                            onOpenCalculator = { navigateTo(ROUTE_KALKULATOR) },
                            onOpenLaporan = { navigateTo(ROUTE_LAPORAN) },
                            onOpenProfileSettings = { showProfileSheet = true },
                            onViewAllPenerimaan = { navigateTo(ROUTE_PENERIMAAN) },
                            onViewAllMustahik = { navigateTo(ROUTE_MUSTAHIK) },
                            onSignOutClick = { showSignOutDialog = true },
                        )
                    }
                    composable(ROUTE_PENERIMAAN) {
                        PenerimaanScreen(
                            transactionsState = transactionsState,
                            officerName = activeOfficerName,
                            isSaving = isSaving,
                            onAddZakatClick = {
                                viewModel.setZakatPrefill(null)
                                editingTransaction = null
                                showAddZakatSheet = true
                            },
                            onEditTransaction = { tx ->
                                editingTransaction = tx
                                showAddZakatSheet = true
                            },
                            onVerifyTransaction = { id, kwitansi ->
                                viewModel.verifyZakatTransaction(id, kwitansi)
                            },
                            onDeleteTransaction = { id, nama ->
                                viewModel.deleteZakatTransaction(id, nama)
                            },
                        )
                    }
                    composable(ROUTE_MUSTAHIK) {
                        MustahikScreen(
                            mustahikState = mustahikState,
                            officerName = activeOfficerName,
                            isSaving = isSaving,
                            onAddMustahikClick = {
                                editingMustahik = null
                                showAddMustahikSheet = true
                            },
                            onEditMustahik = { record ->
                                editingMustahik = record
                                showAddMustahikSheet = true
                            },
                            onDistributeMustahik = { id, nama, uang, beras, catatan ->
                                viewModel.distributeMustahik(id, nama, uang, beras, catatan)
                            },
                            onDeleteMustahik = { id, nama ->
                                viewModel.deleteMustahik(id, nama)
                            },
                        )
                    }
                    composable(ROUTE_LAPORAN) {
                        LaporanScreen(
                            profileState = profileState,
                            transactionsState = transactionsState,
                            mustahikState = mustahikState,
                            summary = summary,
                            onShowMessage = { msg ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            },
                        )
                    }
                    composable(ROUTE_KALKULATOR) {
                        KalkulatorScreen(
                            profile = activeProfile,
                            onRecordCalculatedZakat = { prefill ->
                                editingTransaction = null
                                viewModel.setZakatPrefill(prefill)
                                showAddZakatSheet = true
                            },
                        )
                    }
                }
            }
        }
    }

    if (showAddZakatSheet) {
        AddEditZakatSheet(
            existingTx = editingTransaction,
            prefill = zakatPrefill,
            hargaBerasPerKg = hargaBerasPerKg,
            isSaving = isSaving,
            onDismiss = {
                showAddZakatSheet = false
                editingTransaction = null
                viewModel.setZakatPrefill(null)
            },
            onSubmitNew = { nama, rtRw, jenis, jiwa, uang, beras, metode, catatan ->
                viewModel.addZakatTransaction(
                    namaMuzakki = nama,
                    rtRw = rtRw,
                    jenisZakat = jenis,
                    jumlahJiwa = jiwa,
                    nominalUang = uang,
                    beratBerasKg = beras,
                    metodePembayaran = metode,
                    catatan = catatan,
                    onComplete = {
                        showAddZakatSheet = false
                        viewModel.setZakatPrefill(null)
                    },
                )
            },
            onSubmitEdit = { updatedTx ->
                viewModel.updateZakatTransaction(updatedTx) {
                    showAddZakatSheet = false
                    editingTransaction = null
                }
            },
        )
    }

    if (showAddMustahikSheet) {
        AddEditMustahikSheet(
            existingMustahik = editingMustahik,
            isSaving = isSaving,
            onDismiss = {
                showAddMustahikSheet = false
                editingMustahik = null
            },
            onSubmitNew = { nama, asnaf, rtRw, uang, beras, catatan ->
                viewModel.addMustahik(
                    namaMustahik = nama,
                    golonganAsnaf = asnaf,
                    rtRw = rtRw,
                    bantuanUang = uang,
                    bantuanBerasKg = beras,
                    catatan = catatan,
                    onComplete = {
                        showAddMustahikSheet = false
                        editingMustahik = null
                    },
                )
            },
            onSubmitEdit = { updated ->
                viewModel.updateMustahik(updated) {
                    showAddMustahikSheet = false
                    editingMustahik = null
                }
            },
        )
    }

    if (showProfileSheet) {
        ProfileSettingsSheet(
            profile = activeProfile,
            isSaving = isSaving,
            onDismiss = { showProfileSheet = false },
            onSave = { nama, jabatan, dusun, targetBeras, targetUang, hargaBeras ->
                viewModel.updateProfile(
                    namaPetugas = nama,
                    jabatan = jabatan,
                    dusun = dusun,
                    targetFitrahBerasKg = targetBeras,
                    targetZakatUang = targetUang,
                    hargaBerasPerKg = hargaBeras,
                    onComplete = { showProfileSheet = false },
                )
            },
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Keluar dari Akun UPZ?") },
            text = { Text("Anda akan kembali ke halaman login petugas UPZ Desa Cikulak Kidul.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        signOutUser(credentialManager, coroutineScope)
                    },
                ) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Batal")
                }
            },
        )
    }
}
