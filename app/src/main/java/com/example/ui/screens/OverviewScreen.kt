package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.model.Category
import com.example.data.model.TransactionType
import com.example.ui.utils.CurrencyUtils
import com.example.ui.utils.IconsHelper
import com.example.ui.viewmodels.MainViewModel

@Composable
fun OverviewScreen(viewModel: MainViewModel, navController: NavController) {
    val month by viewModel.currentMonth.collectAsState()
    val year by viewModel.currentYear.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    
    val transactions = allTransactions.filter {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
        cal.get(java.util.Calendar.MONTH) == month && cal.get(java.util.Calendar.YEAR) == year
    }
    val categories by viewModel.categories.collectAsState()
    val incognito by viewModel.incognitoMode.collectAsState()

    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when(themeMode) {
        1 -> false
        2 -> true
        else -> isSystemDark
    }

    val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount * it.exchangeRate }
    val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount * it.exchangeRate }
    val balance = income - expense

    val bgColor = if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // 2.1. Фіксований заголовок: Перемикач місяців (Month Selector)
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            CustomMonthYearSelector(viewModel, month, year, isDark)
        }
        
        // Скролінг (Нижня частина)
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(bottom = 128.dp)
        ) {
            // 2.2. Картка Балансу (Balance Card)
            item {
                BalanceCard(balance, incognito, isDark, "UAH")
            }
            
            // 2.3. Списки Транзакцій: Витрати
            val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
            if (expenseCategories.isNotEmpty() || expense > 0) {
                item {
                    CategoryListCard(
                        title = "Витрати",
                        isIncome = false,
                        categories = expenseCategories,
                        transactions = transactions,
                        month = month,
                        year = year,
                        incognito = incognito,
                        navController = navController,
                        viewModel = viewModel,
                        isDark = isDark
                    )
                }
            }
            
            // 2.3. Списки Транзакцій: Доходи
            val incomeCategories = categories.filter { it.type == TransactionType.INCOME }
            if (incomeCategories.isNotEmpty() || income > 0) {
                item {
                    CategoryListCard(
                        title = "Доходи",
                        isIncome = true,
                        categories = incomeCategories,
                        transactions = transactions,
                        month = month,
                        year = year,
                        incognito = incognito,
                        navController = navController,
                        viewModel = viewModel,
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
fun CustomMonthYearSelector(viewModel: MainViewModel, month: Int, year: Int, isDark: Boolean) {
    val cardBg = if (isDark) Color(0xFF1F2937) else Color.White
    val textColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
    val iconColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF4B5563)
    val rippleColor = if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)
    
    val monthNames = listOf("Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень", "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень")
    val monthName = monthNames.getOrNull(month) ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.previousMonth() },
                modifier = Modifier.clip(CircleShape).background(rippleColor)
            ) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Попередній місяць", tint = iconColor)
            }
            
            Text(
                text = "${monthName.replaceFirstChar { it.uppercaseChar() }} $year",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
            
            IconButton(
                onClick = { viewModel.nextMonth() },
                modifier = Modifier.clip(CircleShape).background(rippleColor)
            ) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Наступний місяць", tint = iconColor)
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double, incognito: Boolean, isDark: Boolean, currency: String = "") {
    val cardBg = if (isDark) Color(0xFF1F2937) else Color.White
    
    val balanceColor = if (balance >= 0) {
        if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
    } else {
        if (isDark) Color(0xFFF87171) else Color(0xFFEF4444)
    }
    
    val prefix = if (balance >= 0) "+" else "-"
    val absoluteBalance = Math.abs(balance)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${prefix.takeIf { it.isNotEmpty() }?.plus(" ") ?: ""}${CurrencyUtils.formatAmount(absoluteBalance, currency, incognito)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    color = balanceColor
                )
            }
        }
    }
}

@Composable
fun CategoryListCard(
    title: String,
    isIncome: Boolean,
    categories: List<Category>,
    transactions: List<com.example.data.model.TransactionEntity>,
    month: Int,
    year: Int,
    incognito: Boolean,
    navController: NavController,
    viewModel: MainViewModel,
    isDark: Boolean
) {
    val cardBg = if (isDark) Color(0xFF1F2937) else Color.White
    val headerBg = if (isDark) Color(0x80374151) else Color(0x80F9FAFB)
    val dividerColor = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
    
    val totalSpent = transactions.filter { it.type == (if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE) }.sumOf { it.amount * it.exchangeRate }
    val totalBudget = categories.sumOf { it.getLimitForMonth(month, year) }
    
    val diff = totalSpent - totalBudget
    val diffText = if (diff > 0) "+${CurrencyUtils.formatAmount(Math.abs(diff), "", incognito)}" else CurrencyUtils.formatAmount(Math.abs(diff), "", incognito)
    
    val headerIconBg = if (isIncome) Color(0x1A10B981) else Color(0x1AEF4444)
    val headerIconColor = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)
    val headerIcon = if (isIncome) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
    
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(headerIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(headerIcon, contentDescription = null, tint = headerIconColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if(isDark) Color.White else Color(0xFF111827))
                        Text(CurrencyUtils.formatAmount(totalBudget, "", incognito), fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(CurrencyUtils.formatAmount(totalSpent, "", incognito), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if(isDark) Color.White else Color(0xFF111827))
                    
                    val diffColor = if (isIncome) {
                        if (diff > 0) Color(0xFF10B981) else Color(0xFF9CA3AF)
                    } else {
                        if (diff > 0) Color(0xFFEF4444) else Color(0xFF9CA3AF)
                    }
                    if (totalBudget > 0) {
                        Text(diffText, fontSize = 11.sp, color = diffColor)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(dividerColor))
            
            // Items
            categories.forEachIndexed { index, cat ->
                val spent = transactions.filter { it.categoryId == cat.id }.sumOf { it.amount * it.exchangeRate }
                val budget = cat.getLimitForMonth(month, year)
                
                CategoryItemRow(
                    cat = cat,
                    spent = spent,
                    budget = budget,
                    isIncome = isIncome,
                    incognito = incognito,
                    isDark = isDark,
                    onClick = {
                        viewModel.setFilterCategoryId(cat.id)
                        viewModel.setFilterAccountId(null)
                        navController.navigate("transactions") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                
                if (index < categories.size - 1) {
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(dividerColor))
                }
            }
        }
    }
}

@Composable
fun CategoryItemRow(
    cat: Category,
    spent: Double,
    budget: Double,
    isIncome: Boolean,
    incognito: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val titleColor = if (isDark) Color.White else Color(0xFF374151)
    val amountColor = if (isDark) Color.White else Color(0xFF111827)
    val trackColor = if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)
    
    val catColor = Color(cat.color.toInt())
    
    val progress = if (budget > 0) (spent / budget).toFloat().coerceAtMost(1f) else if (spent > 0) 1f else 0f
    val diff = spent - budget
    val diffText = if (diff > 0) "+${CurrencyUtils.formatAmount(Math.abs(diff), "", incognito)}" else CurrencyUtils.formatAmount(Math.abs(diff), "", incognito)
    
    val diffColor = if (isIncome) {
        if (diff > 0) Color(0xFF10B981) else Color(0xFF9CA3AF)
    } else {
        if (diff > 0) Color(0xFFEF4444) else Color(0xFF9CA3AF)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(catColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(IconsHelper.getIcon(cat.iconName), contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = cat.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Text(
                    text = CurrencyUtils.formatAmount(spent, "", incognito),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = catColor,
                trackColor = trackColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = CurrencyUtils.formatAmount(budget, "", incognito),
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
                if (budget > 0) {
                    Text(
                        text = diffText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = diffColor
                    )
                }
            }
        }
    }
}

