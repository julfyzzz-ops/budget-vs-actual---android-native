package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle
import com.example.data.model.Category
import com.example.data.model.TransactionType
import com.example.ui.utils.CurrencyUtils
import com.example.ui.utils.IconsHelper
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.GlassTheme.glassyCard
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(viewModel: MainViewModel, onEditTransaction: (com.example.data.model.TransactionEntity) -> Unit) {
    val month by viewModel.currentMonth.collectAsState()
    val year by viewModel.currentYear.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val filterAccountId by viewModel.filterAccountId.collectAsState()
    val filterCategoryId by viewModel.filterCategoryId.collectAsState()

    val transactions = allTransactions.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        val matchesMonth = cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
        val matchesAccount = filterAccountId?.let { id -> it.accountId == id || it.receiverAccountId == id } ?: true
        val matchesCategory = filterCategoryId?.let { id -> it.categoryId == id } ?: true
        matchesMonth && matchesAccount && matchesCategory
    }
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val incognito by viewModel.incognitoMode.collectAsState()
    
    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when(themeMode) {
        1 -> false
        2 -> true
        3 -> false // Android 17 Light Glass
        4 -> true  // Android 17 Dark Glass
        else -> isSystemDark
    }

    val isGlassMode = themeMode == 3 || themeMode == 4
    val bgColor = if (isGlassMode) Color.Transparent else if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)
    val floatBgColor = if (isDark) Color(0xCC374151) else Color(0xCCFFFFFF)
    val floatBorderColor = if (isDark) Color(0xFF4B5563) else Color(0xFFE5E7EB)

    val isFilterActive = filterAccountId != null || filterCategoryId != null

    var transactionToDelete by remember { mutableStateOf<com.example.data.model.TransactionEntity?>(null) }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Підтвердження видалення", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF111827)) },
            text = { Text("Ви впевнені, що хочете видалити цю транзакцію?", color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Видалити", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Скасувати", color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280))
                }
            },
            containerColor = if (isDark) Color(0xFF1F2937) else Color.White
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(bgColor)) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            CustomMonthYearSelector(viewModel, month, year, isDark)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = if (isGlassMode) {
                    Modifier
                        .fillMaxWidth()
                        .glassyCard(RoundedCornerShape(12.dp))
                        .padding(4.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .background(if(isDark) Color(0xFF1F2937) else Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, floatBorderColor, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                var accMenuExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    TextButton(onClick = { accMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (filterAccountId != null) accounts.find { it.id == filterAccountId }?.name ?: "Рахунок" else "Всі рахунки",
                            color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    DropdownMenu(expanded = accMenuExpanded, onDismissRequest = { accMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Всі рахунки") }, onClick = { viewModel.setFilterAccountId(null); accMenuExpanded = false })
                        accounts.forEach { acc ->
                            DropdownMenuItem(text = { Text(acc.name) }, onClick = { viewModel.setFilterAccountId(acc.id); accMenuExpanded = false })
                        }
                    }
                }
                
                Box(
                    modifier = Modifier.width(1.dp).height(24.dp).background(floatBorderColor)
                )
                
                var catMenuExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    TextButton(onClick = { catMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (filterCategoryId != null) categories.find { it.id == filterCategoryId }?.name ?: "Категорія" else "Всі категорії",
                            color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    DropdownMenu(expanded = catMenuExpanded, onDismissRequest = { catMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Всі категорії") }, onClick = { viewModel.setFilterCategoryId(null); catMenuExpanded = false })
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = { viewModel.setFilterCategoryId(cat.id); catMenuExpanded = false })
                        }
                    }
                }
                
                if (isFilterActive) {
                    IconButton(
                        onClick = {
                            viewModel.setFilterAccountId(null)
                            viewModel.setFilterCategoryId(null)
                        },
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFE4E6))
                    ) {
                        Icon(Icons.Filled.Close, tint = Color(0xFFE11D48), contentDescription = "Скинути", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        
        // Floating controls + List
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // List
                if (transactions.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Немає транзакцій", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 128.dp)
                    ) {
                        val grouped = transactions.groupBy { 
                            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                            val df = SimpleDateFormat("EEEE, d MMM", Locale("uk", "UA"))
                            df.format(cal.time).uppercase()
                        }
                        
                        grouped.forEach { (dateStr, txs) ->
                            stickyHeader {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bgColor.copy(alpha = 0.95f))
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF6B7280)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dateStr,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                            item {
                                val cardBg = if (isGlassMode) Color.Transparent else if (isDark) Color(0xFF1F2937) else Color.White
                                val borderColor = if (isGlassMode) Color(0x1AE5E7EB) else if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
                                
                                Column(
                                    modifier = if (isGlassMode) {
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                            .glassyCard(RoundedCornerShape(12.dp))
                                    } else {
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(cardBg)
                                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    }
                                ) {
                                    txs.forEachIndexed { index, tx ->
                                        val cat = categories.find { it.id == tx.categoryId }
                                        val acc = accounts.find { it.id == tx.accountId }
                                        val receiverAcc = tx.receiverAccountId?.let { id -> accounts.find { it.id == id } }
                                        
                                        TransactionRowItem(
                                            tx = tx,
                                            category = cat,
                                            account = acc,
                                            receiverAccount = receiverAcc,
                                            incognito = incognito,
                                            isDark = isDark,
                                            onEditClick = { onEditTransaction(tx) },
                                            onDelete = { transactionToDelete = tx }
                                        )
                                        
                                        if (index < txs.size - 1) {
                                            Spacer(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(borderColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Floating Controls handled above
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionRowItem(
    tx: com.example.data.model.TransactionEntity,
    category: Category?,
    account: com.example.data.model.Account?,
    receiverAccount: com.example.data.model.Account?,
    incognito: Boolean,
    isDark: Boolean,
    onEditClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isTransfer = tx.type == TransactionType.TRANSFER

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                return@rememberSwipeToDismissBoxState false
            } else if (it == SwipeToDismissBoxValue.StartToEnd) {
                onEditClick()
                return@rememberSwipeToDismissBoxState false
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            if (direction != SwipeToDismissBoxValue.Settled) {
                val color = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFE11D48)
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF2563EB)
                    else -> Color.Transparent
                }
                val alignment = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.Center
                }
                val icon = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
                    SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Edit
                    else -> Icons.Filled.Delete
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
            }
        },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF1F2937) else Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left - Icon
                val catColor = if (isTransfer) Color(0xFF3B82F6) else category?.color?.let { Color(it.toInt()) } ?: Color(0xFF9CA3AF)
                val icon = if (isTransfer) Icons.Filled.SwapHoriz else IconsHelper.getIcon(category?.iconName ?: "help")
                
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(catColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Middle - Text
                Column(modifier = Modifier.weight(1f)) {
                    val title = if (isTransfer) "Переказ коштів" else category?.name ?: "Невідомо"
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    val accText = if (isTransfer && receiverAccount != null) {
                        "${account?.name ?: "—"} → ${receiverAccount.name}"
                    } else {
                        account?.name ?: "—"
                    }
                    
                    val bottomText = if (tx.note.isNotEmpty()) "$accText • ${tx.note}" else accText
                    Text(
                        text = bottomText,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontStyle = if (tx.note.isNotEmpty()) FontStyle.Italic else FontStyle.Normal
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Right - Amount
                if (isTransfer && receiverAccount != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        val absoluteAmount = Math.abs(tx.amount)
                        val redAmt = "- ${CurrencyUtils.formatAmount(absoluteAmount, tx.currency, incognito)}"
                        Text(
                            text = redAmt,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        
                        val recAmtVal = tx.receiverAmount ?: absoluteAmount
                        val greenAmt = "+ ${CurrencyUtils.formatAmount(recAmtVal, receiverAccount.currency, incognito)}"
                        Text(
                            text = greenAmt,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                } else {
                    val isIncome = tx.type == TransactionType.INCOME
                    val isPositiveEffect = (isIncome && tx.amount >= 0) || (!isIncome && tx.amount < 0)
                    
                    val amountColor = if (isPositiveEffect) {
                        Color(0xFF10B981)
                    } else {
                        if (isDark) Color.White else Color(0xFF111827)
                    }
                    
                    val prefix = if (isPositiveEffect) "+" else if (tx.amount != 0.0) "-" else ""
                    val absoluteAmount = Math.abs(tx.amount)
                    val amtStr = "${prefix.takeIf { it.isNotEmpty() }?.plus(" ") ?: ""}${CurrencyUtils.formatAmount(absoluteAmount, tx.currency, incognito)}"
                    
                    Text(
                        text = amtStr,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = amountColor
                    )
                }
            }
        }
    )
}
