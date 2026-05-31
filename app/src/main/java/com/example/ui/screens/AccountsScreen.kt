package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.model.AccountType
import com.example.data.model.Account
import com.example.ui.utils.CurrencyUtils
import com.example.ui.utils.IconsHelper
import com.example.ui.viewmodels.MainViewModel

@Composable
fun AccountsScreen(
    viewModel: MainViewModel, 
    navController: NavController, 
    onOpenSettings: () -> Unit,
    onOpenExperimentalSettings: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val incognito by viewModel.incognitoMode.collectAsState()
    
    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when(themeMode) {
        1 -> false
        2 -> true
        else -> isSystemDark
    }

    var showAddAccountSheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var accountToEdit by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Account?>(null) }
    var showHiddenAccounts by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    if (showAddAccountSheet) {
        AddEditAccountSheet(
            viewModel = viewModel,
            accountToEdit = accountToEdit,
            onDismissInfo = { 
                showAddAccountSheet = false
                accountToEdit = null
            }
        )
    }
    
    fun getDynamicBalance(account: Account): Double {
        return viewModel.getAccountBalance(account.id)
    }

    val totalCapital = accounts.sumOf { getDynamicBalance(it) * it.exchangeRate }
    val bgColor = if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)

    Column(modifier = Modifier.fillMaxSize().background(bgColor)) {
        // Top Card Selector
        val topCardBg = if (isDark) Color(0xFF1F2937) else Color.White
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = topCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Бюджет",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF111827)
                        )
                        Text(
                            text = "v1.4.7 (31.05.2026 09:20)",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Red Bank Import Settings Button
                        IconButton(
                            onClick = onOpenExperimentalSettings,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFEF4444), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Імпорт з банківських пушів",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Regular Settings Button
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Налаштування",
                                tint = if (isDark) Color.White else Color(0xFF374151),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main List
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 128.dp)
        ) {
            // Total Capital
            item {
                BalanceCard(totalCapital, incognito, isDark, "UAH")
            }

            val currentAccs = accounts.filter { it.type == AccountType.CURRENT }.sortedBy { it.orderIndex }
            val savingsAccs = accounts.filter { it.type == AccountType.SAVINGS }.sortedBy { it.orderIndex }
            val debtAccs = accounts.filter { it.type == AccountType.DEBT }.sortedBy { it.orderIndex }

            if (currentAccs.isNotEmpty()) {
                item {
                    val groupSum = currentAccs.sumOf { getDynamicBalance(it) * it.exchangeRate }
                    AccountGroup(
                        title = "Поточні рахунки",
                        icon = Icons.Filled.AccountBalanceWallet,
                        iconBg = Color(0xFFDBEAFE),
                        iconTint = Color(0xFF2563EB),
                        accounts = currentAccs,
                        groupSum = groupSum,
                        viewModel = viewModel,
                        incognito = incognito,
                        showHiddenAccounts = showHiddenAccounts,
                        isDark = isDark,
                        navController = navController,
                        onEditClick = { acc -> accountToEdit = acc; showAddAccountSheet = true },
                        getDynamicBalance = { getDynamicBalance(it) }
                    )
                }
            }

            if (savingsAccs.isNotEmpty()) {
                item {
                    val groupSum = savingsAccs.sumOf { getDynamicBalance(it) * it.exchangeRate }
                    AccountGroup(
                        title = "Заощадження",
                        icon = Icons.Filled.Savings,
                        iconBg = Color(0xFFD1FAE5),
                        iconTint = Color(0xFF10B981),
                        accounts = savingsAccs,
                        groupSum = groupSum,
                        viewModel = viewModel,
                        incognito = incognito,
                        showHiddenAccounts = showHiddenAccounts,
                        isDark = isDark,
                        navController = navController,
                        onEditClick = { acc -> accountToEdit = acc; showAddAccountSheet = true },
                        getDynamicBalance = { getDynamicBalance(it) }
                    )
                }
            }

            if (debtAccs.isNotEmpty()) {
                item {
                    val groupSum = debtAccs.sumOf { getDynamicBalance(it) * it.exchangeRate }
                    AccountGroup(
                        title = "Борги/Кредити",
                        icon = Icons.Filled.CreditCard,
                        iconBg = Color(0xFFFEE2E2),
                        iconTint = Color(0xFFEF4444),
                        accounts = debtAccs,
                        groupSum = groupSum,
                        viewModel = viewModel,
                        incognito = incognito,
                        showHiddenAccounts = showHiddenAccounts,
                        isDark = isDark,
                        navController = navController,
                        onEditClick = { acc -> accountToEdit = acc; showAddAccountSheet = true },
                        getDynamicBalance = { getDynamicBalance(it) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF1F2937) else Color.White)
                        .clickable { 
                            accountToEdit = null
                            showAddAccountSheet = true 
                        }
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Новий рахунок",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White else Color(0xFF111827)
                    )
                }
            }
            
            item {
                val isVisible = showHiddenAccounts
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF1F2937) else Color.White)
                        .clickable { showHiddenAccounts = !showHiddenAccounts }
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (isVisible) "Приховати рахунки" else "Відобразити всі рахунки",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White else Color(0xFF111827)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun AccountGroup(
    title: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    accounts: List<Account>,
    groupSum: Double,
    viewModel: MainViewModel,
    incognito: Boolean,
    showHiddenAccounts: Boolean,
    isDark: Boolean,
    navController: NavController,
    onEditClick: (Account) -> Unit,
    getDynamicBalance: (Account) -> Double
) {
    if (accounts.isEmpty()) return

    val cardBg = if (isDark) Color(0xFF1F2937) else Color.White
    val borderColor = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            val headerBg = if (isDark) Color(0x80374151) else Color(0x80F9FAFB)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = CurrencyUtils.formatAmount(groupSum, "UAH", incognito),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )
            }
            
            Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(borderColor))
            
            // Items
            if (accounts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Немає рахунків", color = if(isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280), fontSize = 14.sp)
                }
            } else {
                accounts.forEachIndexed { index, account ->
                    AccountRowItem(
                        account = account,
                        balance = getDynamicBalance(account),
                        viewModel = viewModel,
                        incognito = incognito,
                        showHiddenAccounts = showHiddenAccounts,
                        isDark = isDark,
                        isFirst = index == 0,
                        isLast = index == accounts.size - 1,
                        onEditClick = { onEditClick(account) },
                        onReorder = { acc, up ->
                            val sameTypeAccs = accounts.toMutableList()
                            val accIdx = sameTypeAccs.indexOfFirst { it.id == acc.id }
                            if (up && accIdx > 0) {
                                val temp = sameTypeAccs[accIdx]
                                sameTypeAccs[accIdx] = sameTypeAccs[accIdx - 1]
                                sameTypeAccs[accIdx - 1] = temp
                            } else if (!up && accIdx < sameTypeAccs.size - 1) {
                                val temp = sameTypeAccs[accIdx]
                                sameTypeAccs[accIdx] = sameTypeAccs[accIdx + 1]
                                sameTypeAccs[accIdx + 1] = temp
                            }
                            
                            sameTypeAccs.forEachIndexed { idx, item ->
                                viewModel.updateAccount(item.copy(orderIndex = idx))
                            }
                        },
                        onClick = {
                            if (!account.isHidden) {
                                viewModel.setFilterAccountId(account.id)
                                viewModel.setFilterCategoryId(null)
                                navController.navigate("transactions") { 
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true 
                                    restoreState = true
                                }
                            }
                        }
                    )
                    if (index < accounts.size - 1 && (!account.isHidden || showHiddenAccounts)) {
                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(borderColor))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountRowItem(
    account: Account,
    balance: Double,
    viewModel: MainViewModel,
    incognito: Boolean,
    showHiddenAccounts: Boolean,
    isDark: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onEditClick: () -> Unit,
    onReorder: (Account, Boolean) -> Unit,
    onClick: () -> Unit
) {
    if (account.isHidden && !showHiddenAccounts) return
    
    var showDeleteConfirm by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Видалити рахунок?") },
            text = { Text("Цей процес незворотній. Всі транзакції залишаться в історії, але рахунку більше не буде.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAccount(account); showDeleteConfirm = false }) { Text("Видалити", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Скасувати") }
            }
        )
    }

    var accumulatedDrag by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0f) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd) {
                onEditClick()
                return@rememberSwipeToDismissBoxState false
            } else if (it == SwipeToDismissBoxValue.EndToStart) {
                showDeleteConfirm = true
                return@rememberSwipeToDismissBoxState false
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            if (direction == SwipeToDismissBoxValue.StartToEnd) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFDBEAFE)).padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Редагувати", tint = Color(0xFF2563EB))
                }
            } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFFFE4E6)).padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Видалити", tint = Color(0xFFE11D48))
                }
            }
        },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF1F2937) else Color.White)
                    .pointerInput(account.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { accumulatedDrag = 0f },
                            onDragEnd = { accumulatedDrag = 0f },
                            onDragCancel = { accumulatedDrag = 0f }
                        ) { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += dragAmount.y
                            if (accumulatedDrag > 100f) {
                                onReorder(account, false)
                                accumulatedDrag = 0f
                            } else if (accumulatedDrag < -100f) {
                                onReorder(account, true)
                                accumulatedDrag = 0f
                            }
                        }
                    }
                    .clickable(onClick = onClick)
                    .alpha(if (account.isHidden) 0.5f else 1f)
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(account.color.toInt())),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(IconsHelper.getIcon(account.iconName), contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = account.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (account.isHidden) {
                            Box(modifier = Modifier.background(Color(0xFFE5E7EB), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                Text("Приховано", fontSize = 10.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Text(
                        text = account.currency,
                        fontSize = 12.sp,
                        color = if(isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatAmount(balance, account.currency, incognito),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (balance < 0) Color(0xFFEF4444) else (if (isDark) Color.White else Color(0xFF111827))
                    )
                    
                    if (account.creditLimit > 0) {
                        val available = account.creditLimit + balance
                        Text("Доступно: ${CurrencyUtils.formatAmount(available, account.currency, incognito)}", fontSize = 12.sp, color = if(isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280))
                    } else if (account.currency != "UAH") {
                        val uahEquivalent = balance * account.exchangeRate
                        Text("~ ${CurrencyUtils.formatAmount(uahEquivalent, "UAH", incognito)}", fontSize = 12.sp, color = if(isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280))
                    }
                }
            }
        }
    )
}

