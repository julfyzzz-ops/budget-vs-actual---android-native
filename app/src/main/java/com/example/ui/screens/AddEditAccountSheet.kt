package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.ui.utils.IconsHelper
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountSheet(
    viewModel: MainViewModel,
    accountToEdit: Account? = null,
    onDismissInfo: () -> Unit
) {
    var name by remember { mutableStateOf(accountToEdit?.name ?: "") }
    var balance by remember { mutableStateOf(accountToEdit?.initialBalance?.toString() ?: "") }
    var currency by remember { mutableStateOf(accountToEdit?.currency ?: "UAH") }
    var type by remember { mutableStateOf(accountToEdit?.type ?: AccountType.CURRENT) }
    var isHidden by remember { mutableStateOf(accountToEdit?.isHidden ?: false) }
    var creditLimit by remember { mutableStateOf(accountToEdit?.creditLimit?.toString() ?: "") }
    
    val presetColors = listOf(
        Color(0xFF10B981), // emerald-500
        Color(0xFF3B82F6), // blue-500
        Color(0xFFEF4444), // red-500
        Color(0xFFF97316), // orange-500
        Color(0xFF8B5CF6), // violet-500
        Color(0xFFEC4899), // pink-500
        Color(0xFF111827), // black
        Color(0xFF6B7280)  // gray-500
    )
    var selectedColor by remember { mutableStateOf(accountToEdit?.color?.let { Color(it.toInt()) } ?: presetColors[0]) }
    
    val desktopIcons = listOf(
        "account_balance_wallet", "credit_card", "payments", "account_balance", 
        "savings", "monetization_on", "business_center", "lock",
        "landmark", "piggy_bank", "briefcase", "vault", "coins"
    )
    var selectedIcon by remember { mutableStateOf(accountToEdit?.iconName ?: desktopIcons[0]) }

    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when(themeMode) {
        1 -> false
        2 -> true
        else -> isSystemDark
    }
    
    val sheetBg = if (isDark) Color(0xFF1F2937) else Color.White
    val inputBg = if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)
    val inputTextColor = if (isDark) Color.White else Color(0xFF111827)
    val labelColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color.Transparent,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedContainerColor = inputBg,
        focusedContainerColor = inputBg,
        focusedTextColor = inputTextColor,
        unfocusedTextColor = inputTextColor
    )
    
    val currencies = listOf("UAH", "USD", "EUR", "PLN", "GBP")
    val types = listOf(
        AccountType.CURRENT to "Поточний",
        AccountType.SAVINGS to "Ощадний",
        AccountType.DEBT to "Борговий"
    )

    // Auto-replace white color with white/black adapting
    val displayColors = presetColors.map {
        if (it == Color(0xFF111827) && !isDark) Color(0xFF111827)
        else if (it == Color(0xFF111827) && isDark) Color.White
        else it
    }

    ModalBottomSheet(
        onDismissRequest = onDismissInfo,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (accountToEdit == null) "Додати рахунок" else "Редагувати рахунок",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = inputTextColor
                )
                
                IconButton(
                    onClick = onDismissInfo,
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Закрити", modifier = Modifier.size(20.dp), tint = labelColor)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Name
            Text("Назва", fontSize = 12.sp, color = labelColor, modifier = Modifier.padding(bottom = 4.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Наприклад: Універсальна, Готівка", color = labelColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Type
            Text("Тип рахунку", fontSize = 12.sp, color = labelColor, modifier = Modifier.padding(bottom = 4.dp))
            var typeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = types.find { it.first == type }?.second ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false },
                    modifier = Modifier.background(inputBg)
                ) {
                    types.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.second, color = inputTextColor) },
                            onClick = {
                                type = selectionOption.first
                                typeExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Balance & Currency Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Баланс", fontSize = 12.sp, color = labelColor, modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = balance,
                        onValueChange = { balance = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("Валюта", fontSize = 12.sp, color = labelColor, modifier = Modifier.padding(bottom = 4.dp))
                    var currencyExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currency,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false },
                            modifier = Modifier.background(inputBg)
                        ) {
                            currencies.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption, color = inputTextColor) },
                                    onClick = {
                                        currency = selectionOption
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Exchange Rate + Credit Limit
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Кредитний ліміт", fontSize = 12.sp, color = labelColor, modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = { creditLimit = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Icon
            Text("Іконка", fontSize = 12.sp, color = labelColor, modifier = Modifier.padding(bottom = 8.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(48.dp),
                modifier = Modifier.height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(desktopIcons) { iconName ->
                    val isSelected = iconName == selectedIcon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else inputBg)
                            .border(if (isSelected) 2.dp else 0.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { selectedIcon = iconName },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            IconsHelper.getIcon(iconName), 
                            contentDescription = null, 
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else labelColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Color
            Text("Колір", fontSize = 12.sp, color = labelColor, modifier = Modifier.padding(bottom = 8.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(40.dp),
                modifier = Modifier.height(50.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(displayColors) { c ->
                    val isSelected = c == selectedColor || (c == Color.White && selectedColor == Color(0xFF111827))
                    val scale by animateFloatAsState(if (isSelected) 1.15f else 1f)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                if (isSelected) labelColor else Color.Transparent,
                                CircleShape
                            )
                            .clickable {
                                val ind = displayColors.indexOf(c)
                                if (ind != -1) {
                                    selectedColor = presetColors[ind]
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(c))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Hidden toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(inputBg)
                    .clickable { isHidden = !isHidden }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Прихований рахунок", fontSize = 14.sp, color = inputTextColor, fontWeight = FontWeight.Medium)
                Switch(
                    checked = isHidden,
                    onCheckedChange = { isHidden = it }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save Button
            Button(
                onClick = {
                    val parsedBalance = balance.toDoubleOrNull() ?: 0.0
                    val parsedLimit = creditLimit.toDoubleOrNull() ?: 0.0
                    val parsedRate = viewModel.getRateForCurrency(currency)
                    
                    if (name.isNotBlank()) {
                        val acc = Account(
                            id = accountToEdit?.id ?: 0,
                            name = name,
                            currency = currency,
                            initialBalance = parsedBalance,
                            type = type,
                            color = selectedColor.toArgb().toLong() and 0xFFFFFFFFL,
                            iconName = selectedIcon,
                            isHidden = isHidden,
                            creditLimit = parsedLimit,
                            exchangeRate = parsedRate,
                            orderIndex = accountToEdit?.orderIndex ?: 0
                        )
                        if (accountToEdit == null) {
                            viewModel.addAccount(acc)
                        } else {
                            viewModel.updateAccount(acc)
                        }
                        onDismissInfo()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (accountToEdit == null) "Створити рахунок" else "Зберегти зміни", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
        }
    }
}
