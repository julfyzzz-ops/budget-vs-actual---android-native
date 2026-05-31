package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.TransactionType
import com.example.ui.utils.IconsHelper
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.GlassTheme.glassyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategorySheet(
    viewModel: MainViewModel,
    categoryToEdit: Category? = null,
    onDismissInfo: () -> Unit
) {
    var name by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    
    val month = viewModel.currentMonth.collectAsState().value
    val year = viewModel.currentYear.collectAsState().value
    val monthNames = listOf("Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень", "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень")
    val monthName = monthNames[month]
    
    var limit by remember { 
        mutableStateOf(
            if (categoryToEdit != null) {
                val catLimit = categoryToEdit.getLimitForMonth(month, year)
                if (catLimit > 0) catLimit.toString() else ""
            } else ""
        )
    }
    
    var type by remember { mutableStateOf(categoryToEdit?.type ?: TransactionType.EXPENSE) }
    
    val presetColors = listOf(
        Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFF59E0B), 
        Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFF6366F1),
        Color(0xFF8B5CF6), Color(0xFFEC4899)
    )
    var selectedColor by remember { mutableStateOf(categoryToEdit?.color?.let { Color(it.toInt()) } ?: presetColors[0]) }
    
    val presetIcons = listOf(
        "shopping_cart", "directions_bus", "restaurant", "local_hospital", 
        "school", "movie", "work", "attach_money", "home", "stethoscope", 
        "paw_print", "more_horizontal", "gem", "film", "baby", "heart", "coins", "briefcase"
    )
    var selectedIcon by remember { mutableStateOf(categoryToEdit?.iconName ?: presetIcons[0]) }

    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when(themeMode) {
        1 -> false
        2 -> true
        3 -> isSystemDark
        else -> isSystemDark
    }

    ModalBottomSheet(
        onDismissRequest = onDismissInfo,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (themeMode == 3) Color.Transparent else if (isDark) Color(0xFF1F2937) else Color.White
    ) {
        val sheetModifier = if (themeMode == 3) {
            Modifier
                .fillMaxWidth()
                .glassyCard(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 16.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        }
        Column(
            modifier = sheetModifier
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (categoryToEdit == null) "Додати категорію" else "Редагувати категорію", 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (themeMode == 3) Color(0x33FFFFFF) else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6))
                        .clickable(onClick = onDismissInfo),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Закрити", tint = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(if (themeMode == 3) Color(0x22111827) else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                val isExpense = type == TransactionType.EXPENSE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .let { 
                            if (isExpense) {
                                if (themeMode == 3) {
                                    it.background(Color(0x33FFFFFF))
                                } else {
                                    it.background(if (isDark) Color(0xFF1F2937) else Color.White).shadow(1.dp, RoundedCornerShape(8.dp))
                                }
                            } else it
                        }
                        .clickable { type = TransactionType.EXPENSE },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Витрати", color = if (isExpense) Color(0xFFEF4444) else (if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .let { 
                            if (!isExpense) {
                                if (themeMode == 3) {
                                    it.background(Color(0x33FFFFFF))
                                } else {
                                    it.background(if (isDark) Color(0xFF1F2937) else Color.White).shadow(1.dp, RoundedCornerShape(8.dp))
                                }
                            } else it
                        }
                        .clickable { type = TransactionType.INCOME },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Доходи", color = if (!isExpense) Color(0xFF10B981) else (if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Назва") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (themeMode == 3) Color(0x33FFFFFF) else if(isDark) Color(0xFF4B5563) else Color(0xFFE5E7EB),
                    focusedBorderColor = Color(0xFF10B981),
                    focusedLabelColor = Color(0xFF10B981),
                    unfocusedContainerColor = if (themeMode == 3) Color(0x22111827) else Color.Transparent,
                    focusedContainerColor = if (themeMode == 3) Color(0x22111827) else Color.Transparent,
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = limit,
                onValueChange = { limit = it },
                label = { Text("Бюджет на $monthName $year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (themeMode == 3) Color(0x33FFFFFF) else if(isDark) Color(0xFF4B5563) else Color(0xFFE5E7EB),
                    focusedBorderColor = Color(0xFF10B981),
                    focusedLabelColor = Color(0xFF10B981),
                    unfocusedContainerColor = if (themeMode == 3) Color(0x22111827) else Color.Transparent,
                    focusedContainerColor = if (themeMode == 3) Color(0x22111827) else Color.Transparent,
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("Колір", fontSize = 14.sp, color = if(isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(44.dp),
                modifier = Modifier.height(50.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(presetColors) { c ->
                    val isSelected = c == selectedColor
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(c)
                            .let {
                                if (isSelected) {
                                    it.border(3.dp, if(isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB), CircleShape)
                                } else it
                            }
                            .clickable { selectedColor = c }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("Іконка", fontSize = 14.sp, color = if(isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(48.dp),
                modifier = Modifier.height(240.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(presetIcons) { iconName ->
                    val isSelected = iconName == selectedIcon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) {
                                    if (themeMode == 3) Color(0x44D1FAE5) else Color(0xFFD1FAE5)
                                } else {
                                    if (themeMode == 3) Color(0x22FFFFFF) else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)
                                }
                            )
                            .let {
                                if (themeMode == 3) {
                                    it.border(1.dp, if (isSelected) Color(0xFF10B981) else Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                } else if (isSelected && !isDark) {
                                    it.border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                                } else it
                            }
                            .clickable { selectedIcon = iconName },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            IconsHelper.getIcon(iconName), 
                            contentDescription = null, 
                            tint = if (isSelected) Color(0xFF10B981) else (if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    val parsedLimit = limit.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        val historyJsonStr = categoryToEdit?.limitHistoryJson ?: "{}"
                        val newHistoryJsonStr = try {
                            val json = org.json.JSONObject(historyJsonStr)
                            json.put(String.format("%04d-%02d", year, month + 1), parsedLimit)
                            json.toString()
                        } catch (e: Exception) {
                            "{\"" + String.format("%04d-%02d", year, month + 1) + "\":" + parsedLimit + "}"
                        }

                        val cat = Category(
                            id = categoryToEdit?.id ?: 0,
                            name = name,
                            type = type,
                            currentLimit = parsedLimit,
                            color = selectedColor.toArgb().toLong() and 0xFFFFFFFFL,
                            iconName = selectedIcon,
                            limitHistoryJson = newHistoryJsonStr,
                            orderIndex = categoryToEdit?.orderIndex ?: 0
                        )
                        if (categoryToEdit == null) {
                            viewModel.addCategory(cat)
                        } else {
                            viewModel.updateCategory(cat)
                        }
                        onDismissInfo()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Зберегти", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

