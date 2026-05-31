package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.ui.screens.*
import com.example.ui.viewmodels.MainViewModel

import com.example.ui.theme.GlassTheme
import com.example.ui.theme.GlassTheme.glassyCard

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Overview : Screen("overview", "Огляд", { Icon(Icons.Filled.GridView, contentDescription = "Огляд") })
    object Transactions : Screen("transactions", "Транз.", { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Транзакції") })
    object Budget : Screen("budget", "Бюджет", { Icon(Icons.Filled.Calculate, contentDescription = "Бюджет") })
    object Accounts : Screen("accounts", "Рахунки", { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Рахунки") })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Overview, Screen.Transactions, Screen.Budget, Screen.Accounts)
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showExperimentalSettingsDialog by remember { mutableStateOf(false) }
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<com.example.data.model.TransactionEntity?>(null) }
    
    val themeMode by viewModel.themeMode.collectAsState()
    val draftTransaction by viewModel.draftTransactionFlow.collectAsState()
    
    LaunchedEffect(draftTransaction) {
        if (draftTransaction != null) {
            showAddTransactionSheet = true
        }
    }
    
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when(themeMode) {
        1 -> false
        2 -> true
        3 -> isSystemDark
        else -> isSystemDark
    }
    
    if (showSettingsDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showSettingsDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = if (themeMode == 3) {
                    Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight()
                        .glassyCard(RoundedCornerShape(24.dp), isDark = isDark)
                } else {
                    Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(24.dp))
                },
                color = if (themeMode == 3) Color.Transparent else if (isDark) Color(0xFF1F2937) else Color.White,
                tonalElevation = if (themeMode == 3) 0.dp else 6.dp
            ) {
                SettingsScreen(viewModel, isExperimental = false, onDismiss = { showSettingsDialog = false })
            }
        }
    }
    
    if (showExperimentalSettingsDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showExperimentalSettingsDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = if (themeMode == 3) {
                    Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight()
                        .glassyCard(RoundedCornerShape(24.dp), isDark = isDark)
                } else {
                    Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(24.dp))
                },
                color = if (themeMode == 3) Color.Transparent else if (isDark) Color(0xFF1F2937) else Color.White,
                tonalElevation = if (themeMode == 3) 0.dp else 6.dp
            ) {
                ExperimentalSettingsScreen(viewModel, onDismiss = { showExperimentalSettingsDialog = false })
            }
        }
    }
    
    if (showAddTransactionSheet) {
        AddTransactionSheet(
            viewModel = viewModel, 
            transactionToEdit = transactionToEdit,
            onDismissInfo = { 
                showAddTransactionSheet = false 
                transactionToEdit = null
                viewModel.setDraftTransaction(null)
            }
        )
    }
    
    val appContent = @Composable {
        Scaffold(
            containerColor = if (themeMode == 3) Color.Transparent else MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = if (themeMode == 3) Color(0x220B0F19) else MaterialTheme.colorScheme.surface,
                    tonalElevation = if (themeMode == 3) 0.dp else 3.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = screen.icon,
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddTransactionSheet = true },
                    containerColor = if (themeMode == 3) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    contentColor = if (themeMode == 3) Color.White else MaterialTheme.colorScheme.onPrimary,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Додати транзакцію")
                }
            }
        ) { innerPadding ->
            NavHost(navController = navController, startDestination = Screen.Overview.route, modifier = Modifier.padding(innerPadding)) {
                composable(Screen.Overview.route) { 
                    OverviewScreen(viewModel, navController) 
                }
                composable(Screen.Transactions.route) { 
                    TransactionsScreen(viewModel, onEditTransaction = {
                        transactionToEdit = it
                        showAddTransactionSheet = true
                    }) 
                }
                composable(Screen.Accounts.route) { 
                    AccountsScreen(
                        viewModel = viewModel, 
                        navController = navController, 
                        onOpenSettings = { showSettingsDialog = true },
                        onOpenExperimentalSettings = { showExperimentalSettingsDialog = true }
                    ) 
                }
                composable(Screen.Budget.route) { BudgetScreen(viewModel) }
            }
        }
    }

    if (themeMode == 3) {
        GlassTheme.GlassBackground(isDark = isDark) {
            appContent()
        }
    } else {
        appContent()
    }
}
