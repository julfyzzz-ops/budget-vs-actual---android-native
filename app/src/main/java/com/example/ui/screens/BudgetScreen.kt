package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.TransactionType
import com.example.ui.utils.CurrencyUtils
import com.example.ui.utils.IconsHelper
import com.example.ui.viewmodels.MainViewModel

@Composable
fun BudgetScreen(viewModel: MainViewModel) {
    val month by viewModel.currentMonth.collectAsState()
    val year by viewModel.currentYear.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val incognito by viewModel.incognitoMode.collectAsState()

    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when(themeMode) {
        1 -> false
        2 -> true
        else -> isSystemDark
    }

    var showAddCategorySheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var categoryToEdit by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Category?>(null) }
    
    if (showAddCategorySheet) {
        AddEditCategorySheet(
            viewModel = viewModel,
            categoryToEdit = categoryToEdit,
            onDismissInfo = { 
                showAddCategorySheet = false
                categoryToEdit = null
            }
        )
    }

    val plnIncome = categories.filter { it.type == TransactionType.INCOME }.sumOf { it.getLimitForMonth(month, year) }
    val plnExpense = categories.filter { it.type == TransactionType.EXPENSE }.sumOf { it.getLimitForMonth(month, year) }
    val projectedBalance = plnIncome - plnExpense

    val bgColor = if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)

    Column(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
            CustomMonthYearSelector(viewModel, month, year, isDark)
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            item {
                val cardBg = if (isDark) Color(0xFF1F2937) else Color.White
                val borderColor = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val pColor = if (projectedBalance >= 0) {
                            if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
                        } else {
                            if (isDark) Color(0xFFF87171) else Color(0xFFEF4444)
                        }
                        val prefix = if (projectedBalance >= 0) "+" else "-"
                        val absBal = Math.abs(projectedBalance)
                        
                        val formatted = CurrencyUtils.formatAmount(absBal, "UAH", incognito)
                        val displayText = "${prefix.takeIf { it.isNotEmpty() }?.plus(" ") ?: ""}$formatted"
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = displayText,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp,
                                color = pColor
                            )
                        }
                    }
                }
            }
            
            val incomes = categories.filter { it.type == TransactionType.INCOME }.sortedBy { it.orderIndex }
            val expenses = categories.filter { it.type == TransactionType.EXPENSE }.sortedBy { it.orderIndex }
            
            if (expenses.isNotEmpty()) {
                item {
                    BudgetGroup(
                        title = "Витрати",
                        categories = expenses,
                        total = plnExpense,
                        isIncome = false,
                        month = month,
                        year = year,
                        viewModel = viewModel,
                        incognito = incognito,
                        isDark = isDark,
                        onEditClick = { cat -> categoryToEdit = cat; showAddCategorySheet = true }
                    )
                }
            }
            
            if (incomes.isNotEmpty()) {
                item {
                    BudgetGroup(
                        title = "Доходи",
                        categories = incomes,
                        total = plnIncome,
                        isIncome = true,
                        month = month,
                        year = year,
                        viewModel = viewModel,
                        incognito = incognito,
                        isDark = isDark,
                        onEditClick = { cat -> categoryToEdit = cat; showAddCategorySheet = true }
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
                            categoryToEdit = null
                            showAddCategorySheet = true 
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
                        text = "Нова категорія", 
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
fun BudgetGroup(
    title: String,
    categories: List<Category>,
    total: Double,
    isIncome: Boolean,
    month: Int,
    year: Int,
    viewModel: MainViewModel,
    incognito: Boolean,
    isDark: Boolean,
    onEditClick: (Category) -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1F2937) else Color.White
    val borderColor = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0x80374151) else Color(0x80F9FAFB))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val iconBg = if (isIncome) Color(0x1A10B981) else Color(0x1AEF4444)
                    val iconTint = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)
                    val headIcon = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
                    
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(headIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDark) Color.White else Color(0xFF111827)
                        )
                        Text(
                            text = "${categories.size} " + (if (isIncome) "категорій доходу" else "категорій витрат"),
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatAmount(total, "", incognito),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDark) Color.White else Color(0xFF111827)
                    )
                    Text(
                        text = "заплановано",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
            
            HorizontalDivider(color = borderColor)
            
            // Items
            categories.forEachIndexed { index, category ->
                BudgetCategoryItem(
                    category = category,
                    month = month,
                    year = year,
                    viewModel = viewModel,
                    incognito = incognito,
                    isDark = isDark,
                    isFirst = index == 0,
                    isLast = index == categories.size - 1,
                    onEditClick = { onEditClick(category) },
                    onReorder = { cat, up ->
                        val sameTypeCategories = categories.toMutableList()
                        val catIdx = sameTypeCategories.indexOfFirst { it.id == cat.id }
                        if (up && catIdx > 0) {
                            val temp = sameTypeCategories[catIdx]
                            sameTypeCategories[catIdx] = sameTypeCategories[catIdx - 1]
                            sameTypeCategories[catIdx - 1] = temp
                        } else if (!up && catIdx < sameTypeCategories.size - 1) {
                            val temp = sameTypeCategories[catIdx]
                            sameTypeCategories[catIdx] = sameTypeCategories[catIdx + 1]
                            sameTypeCategories[catIdx + 1] = temp
                        }
                        
                        sameTypeCategories.forEachIndexed { idx, item ->
                            viewModel.updateCategory(item.copy(orderIndex = idx))
                        }
                    }
                )
                if (index < categories.size - 1) {
                    HorizontalDivider(color = borderColor, modifier = Modifier.padding(start = 64.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCategoryItem(
    category: Category,
    month: Int,
    year: Int,
    viewModel: MainViewModel,
    incognito: Boolean,
    isDark: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onEditClick: () -> Unit,
    onReorder: (Category, Boolean) -> Unit
) {
    var showDeleteConfirm by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Видалити категорію?") },
            text = { Text("Всі пов'язані з нею дані залишаться, але категорія буде видалена з бюджету.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCategory(category); showDeleteConfirm = false }) { Text("Видалити", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Скасувати") }
            }
        )
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { direction ->
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                showDeleteConfirm = true
                return@rememberSwipeToDismissBoxState false
            } else if (direction == SwipeToDismissBoxValue.StartToEnd) {
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
                    else -> Icons.Filled.Edit
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
            var accumulatedDrag by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0f) }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF1F2937) else Color.White)
                    .pointerInput(category.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { accumulatedDrag = 0f },
                            onDragEnd = { accumulatedDrag = 0f },
                            onDragCancel = { accumulatedDrag = 0f }
                        ) { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += dragAmount.y
                            if (accumulatedDrag > 50f && !isLast) {
                                onReorder(category, false)
                                accumulatedDrag = 0f
                            } else if (accumulatedDrag < -50f && !isFirst) {
                                onReorder(category, true)
                                accumulatedDrag = 0f
                            }
                        }
                    }
                    .clickable(onClick = onEditClick)
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(category.color.toInt())),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(IconsHelper.getIcon(category.iconName), contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color.White else Color(0xFF111827),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = CurrencyUtils.formatAmount(category.getLimitForMonth(month, year), "", incognito),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )
            }
        }
    )
}
