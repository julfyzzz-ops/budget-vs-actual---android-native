package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.utils.IconsHelper
import com.example.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.GlassTheme.glassyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    viewModel: MainViewModel,
    transactionToEdit: TransactionEntity? = null,
    onDismissInfo: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    val draft = viewModel.draftTransactionFlow.value
    
    var selectedType by remember { mutableStateOf(transactionToEdit?.type ?: draft?.type ?: TransactionType.EXPENSE) }
    
    var selectedAccount by remember { mutableStateOf<Account?>(accounts.find { it.id == (transactionToEdit?.accountId ?: draft?.accountId) } ?: accounts.firstOrNull()) }
    var selectedTargetAccount by remember { mutableStateOf<Account?>(accounts.find { it.id == transactionToEdit?.receiverAccountId }) }
    
    var amountValue by remember {
        val initialText = transactionToEdit?.amount?.toString() ?: draft?.amount?.toString() ?: ""
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }
    var receiverAmountValue by remember {
        val initialText = transactionToEdit?.receiverAmount?.toString() ?: ""
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }
    var note by remember { mutableStateOf(transactionToEdit?.note ?: draft?.note ?: "") }
    var selectedCategory by remember { mutableStateOf<Category?>(categories.find { it.id == (transactionToEdit?.categoryId ?: draft?.categoryId) }) }
    
    LaunchedEffect(categories, selectedType) {
        if (transactionToEdit == null && draft == null) {
            val currentCats = categories.filter { it.type == selectedType }.sortedBy { it.orderIndex }
            if (selectedCategory == null || selectedCategory?.type != selectedType) {
                selectedCategory = currentCats.firstOrNull()
            }
        } else if (draft != null) {
            if (selectedCategory != null && selectedCategory?.type != selectedType) {
                selectedCategory = categories.filter { it.type == selectedType }.sortedBy { it.orderIndex }.firstOrNull()
            } else if (selectedCategory == null) {
                selectedCategory = categories.find { it.id == draft.categoryId }
            }
        } else {
            if (selectedCategory != null && selectedCategory?.type != selectedType) {
                selectedCategory = categories.filter { it.type == selectedType }.sortedBy { it.orderIndex }.firstOrNull()
            } else if (selectedCategory == null) {
                selectedCategory = categories.find { it.id == transactionToEdit?.categoryId }
            }
        }
    }

    LaunchedEffect(accounts) {
        if (selectedAccount == null && accounts.isNotEmpty()) {
            selectedAccount = accounts.find { it.id == (transactionToEdit?.accountId ?: draft?.accountId) } ?: accounts.firstOrNull()
        }
        if (selectedTargetAccount == null && transactionToEdit?.receiverAccountId != null) {
            selectedTargetAccount = accounts.find { it.id == transactionToEdit.receiverAccountId }
        }
    }
    
    var selectedDate by remember { mutableStateOf(transactionToEdit?.timestamp?.let { Date(it) } ?: Date()) }

    var isAmountFocused by remember { mutableStateOf(false) }
    var isReceiverFocused by remember { mutableStateOf(false) }

    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when(themeMode) {
        1 -> false
        2 -> true
        3 -> isSystemDark
        else -> isSystemDark
    }

    val bgColor = if (isDark) Color(0xFF1F2937) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val mutedColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismissInfo,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (themeMode == 3) Color.Transparent else bgColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        val rootModifier = if (themeMode == 3) {
            Modifier
                .fillMaxWidth()
                .glassyCard(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp, top = 16.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        }
        Column(
            modifier = rootModifier
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (transactionToEdit == null) "Додати транзакцію" else "Редагувати",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (themeMode == 3) Color(0x33FFFFFF) else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6))
                        .clickable(onClick = onDismissInfo),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Закрити", tint = mutedColor, modifier = Modifier.size(18.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Segmented Control
            val segmentedBg = if (themeMode == 3) Color(0x22111827) else if (isDark) Color(0xFF111827) else Color(0xFFF3F4F6)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(segmentedBg, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                val types = listOf(
                    TransactionType.EXPENSE to "Витрата",
                    TransactionType.INCOME to "Дохід",
                    TransactionType.TRANSFER to "Переказ"
                )
                types.forEach { (type, label) ->
                    val isSelected = selectedType == type
                    val activeColor = when (type) {
                        TransactionType.EXPENSE -> Color(0xFFEF4444)
                        TransactionType.INCOME -> Color(0xFF10B981)
                        TransactionType.TRANSFER -> Color(0xFF3B82F6)
                    }
                    val bg = if (isSelected) {
                        if (themeMode == 3) Color(0x33FFFFFF) else if (isDark) Color(0xFF374151) else Color.White
                    } else {
                        Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(if (isSelected && themeMode != 3) 1.dp else 0.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .clickable {
                                selectedType = type
                                if (type == TransactionType.TRANSFER && selectedTargetAccount == null) {
                                    selectedTargetAccount = accounts.find { it.id != selectedAccount?.id } ?: accounts.firstOrNull()
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) activeColor else mutedColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Account & Amount Block
            if (selectedType != TransactionType.TRANSFER) {
                // Single Account Selection
                AccountSelector(
                    account = selectedAccount,
                    accounts = accounts,
                    onAccountSelected = { selectedAccount = it },
                    isDark = isDark,
                    themeMode = themeMode
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Amount Input
                AmountInput(
                    amountValue = amountValue,
                    onAmountChange = { amountValue = it },
                    currency = selectedAccount?.currency ?: "UAH",
                    isDark = isDark,
                    onFocusChange = { isAmountFocused = it },
                    themeMode = themeMode
                )
            } else {
                // Transfer Accounts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AccountSelector(
                            account = selectedAccount,
                            accounts = accounts,
                            onAccountSelected = { selectedAccount = it },
                            isDark = isDark,
                            label = "З рахунку",
                            themeMode = themeMode
                        )
                    }
                    Box(modifier = Modifier.padding(horizontal = 8.dp).padding(top = 16.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = mutedColor)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AccountSelector(
                            account = selectedTargetAccount,
                            accounts = accounts,
                            onAccountSelected = { selectedTargetAccount = it },
                            isDark = isDark,
                            label = "На рахунок",
                            themeMode = themeMode
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedAccount != null && selectedTargetAccount != null && selectedAccount?.currency != selectedTargetAccount?.currency) {
                    // Multi-currency transfer
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Списати (${selectedAccount?.currency})", fontSize = 12.sp, color = mutedColor, modifier = Modifier.padding(bottom = 8.dp))
                        AmountInput(
                            amountValue = amountValue,
                            onAmountChange = { amountValue = it },
                            currency = selectedAccount?.currency ?: "",
                            isDark = isDark,
                            onFocusChange = { isAmountFocused = it },
                            hideCurrencyPrefix = true,
                            themeMode = themeMode
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // A simple divider/exchange rate mock
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = if (themeMode == 3) Color(0x33FFFFFF) else if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB))
                            Text(" Курс обміну ", fontSize = 12.sp, color = mutedColor)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = if (themeMode == 3) Color(0x33FFFFFF) else if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Зарахувати (${selectedTargetAccount?.currency})", fontSize = 12.sp, color = mutedColor, modifier = Modifier.padding(bottom = 8.dp))
                        AmountInput(
                            amountValue = receiverAmountValue,
                            onAmountChange = { receiverAmountValue = it },
                            currency = selectedTargetAccount?.currency ?: "",
                            isDark = isDark,
                            onFocusChange = { isReceiverFocused = it },
                            hideCurrencyPrefix = true,
                            themeMode = themeMode
                        )
                    }
                } else {
                    AmountInput(
                        amountValue = amountValue,
                        onAmountChange = { amountValue = it },
                        currency = selectedAccount?.currency ?: "UAH",
                        isDark = isDark,
                        onFocusChange = { isAmountFocused = it },
                        themeMode = themeMode
                    )
                }
            }
            
            // Inline Calculator
            AnimatedVisibility(visible = isAmountFocused || isReceiverFocused) {
                InlineCalculator(
                    onOperatorClick = { op ->
                        if (isReceiverFocused) {
                            val currentText = receiverAmountValue.text
                            val selectionStart = receiverAmountValue.selection.start.coerceIn(0, currentText.length)
                            val selectionEnd = receiverAmountValue.selection.end.coerceIn(0, currentText.length)
                            val newText = if (selectionStart >= 0 && selectionEnd >= 0) {
                                currentText.substring(0, selectionStart) + op + currentText.substring(selectionEnd)
                            } else {
                                currentText + op
                            }
                            val newCursorPos = if (selectionStart >= 0) selectionStart + op.length else newText.length
                            receiverAmountValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(newCursorPos)
                            )
                        } else {
                            val currentText = amountValue.text
                            val selectionStart = amountValue.selection.start.coerceIn(0, currentText.length)
                            val selectionEnd = amountValue.selection.end.coerceIn(0, currentText.length)
                            val newText = if (selectionStart >= 0 && selectionEnd >= 0) {
                                currentText.substring(0, selectionStart) + op + currentText.substring(selectionEnd)
                            } else {
                                currentText + op
                            }
                            val newCursorPos = if (selectionStart >= 0) selectionStart + op.length else newText.length
                            amountValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(newCursorPos)
                            )
                        }
                    },
                    onEqualClick = {
                        if (isReceiverFocused) {
                            val evaluated = evaluateMath(receiverAmountValue.text)
                            if (evaluated != null) {
                                val fmt = if (evaluated == evaluated.toLong().toDouble()) evaluated.toLong().toString() else evaluated.toString()
                                receiverAmountValue = TextFieldValue(text = fmt, selection = TextRange(fmt.length))
                                focusManager.clearFocus()
                            }
                        } else {
                            val evaluated = evaluateMath(amountValue.text)
                            if (evaluated != null) {
                                val fmt = if (evaluated == evaluated.toLong().toDouble()) evaluated.toLong().toString() else evaluated.toString()
                                amountValue = TextFieldValue(text = fmt, selection = TextRange(fmt.length))
                                focusManager.clearFocus()
                            }
                        }
                    },
                    isDark = isDark,
                    themeMode = themeMode
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Category Selection (Only for Income/Expense)
            if (selectedType != TransactionType.TRANSFER) {
                Text("Категорія", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                Spacer(modifier = Modifier.height(12.dp))
                
                val currentCategories = categories.filter { it.type == selectedType }.sortedBy { it.orderIndex }
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.heightIn(max = 200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentCategories) { cat ->
                        val isSelected = selectedCategory?.id == cat.id
                        val catBaseColor = Color(cat.color.toInt())
                        
                        Box(
                            modifier = if (themeMode == 3) {
                                Modifier
                                    .glassyCard(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) catBaseColor.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { selectedCategory = cat }
                                    .padding(12.dp)
                            } else {
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) catBaseColor.copy(alpha = 0.1f) else Color.Transparent)
                                    .border(1.dp, if (isSelected) catBaseColor else (if(isDark) Color(0xFF374151) else Color(0xFFE5E7EB)), RoundedCornerShape(12.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(12.dp)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(catBaseColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(IconsHelper.getIcon(cat.iconName), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = cat.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) textColor else mutedColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Date picker
            val context = LocalContext.current
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Дата", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = if (themeMode == 3) {
                            Modifier
                                .fillMaxWidth()
                                .glassyCard(RoundedCornerShape(12.dp))
                                .clickable {
                                    val cal = Calendar.getInstance()
                                    cal.time = selectedDate
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val newCal = Calendar.getInstance()
                                            newCal.set(y, m, d)
                                            selectedDate = newCal.time
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF374151) else Color(0xFFF9FAFB))
                                .clickable {
                                    val cal = Calendar.getInstance()
                                    cal.time = selectedDate
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val newCal = Calendar.getInstance()
                                            newCal.set(y, m, d)
                                            selectedDate = newCal.time
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        }
                    ) {
                        Text(dateFormat.format(selectedDate), fontSize = 16.sp, color = textColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note
            Text("Нотатка", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
            Spacer(modifier = Modifier.height(8.dp))
            val noteContainerColor = if (themeMode == 3) Color(0x33111827) else if (isDark) Color(0xFF374151) else Color(0xFFF9FAFB)
            val noteBorderColor = if (themeMode == 3) Color(0x33FFFFFF) else if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Опис...", color = mutedColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = noteBorderColor,
                    focusedContainerColor = noteContainerColor,
                    unfocusedContainerColor = noteContainerColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val evaluatedAmount = evaluateMath(amountValue.text) ?: 0.0
                    
                    if (evaluatedAmount != 0.0 && selectedAccount != null) {
                        val validCategory = selectedType == TransactionType.TRANSFER || selectedCategory != null
                        if (validCategory) {
                            val targetAmount = evaluateMath(receiverAmountValue.text) ?: 0.0
                            
                            val tx = TransactionEntity(
                                id = transactionToEdit?.id ?: 0,
                                amount = evaluatedAmount,
                                currency = selectedAccount?.currency ?: "UAH",
                                accountId = selectedAccount!!.id,
                                categoryId = selectedCategory?.id ?: 0,
                                receiverAccountId = if (selectedType == TransactionType.TRANSFER) selectedTargetAccount?.id else null,
                                receiverAmount = if (selectedType == TransactionType.TRANSFER && selectedAccount?.currency != selectedTargetAccount?.currency) targetAmount.takeIf { it != 0.0 } else null,
                                type = selectedType,
                                timestamp = selectedDate.time,
                                note = note
                            )
                            viewModel.addTransaction(tx) // Upserts via viewmodel handling
                            onDismissInfo()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Зберегти", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AccountSelector(
    account: Account?,
    accounts: List<Account>,
    onAccountSelected: (Account) -> Unit,
    isDark: Boolean,
    label: String? = null,
    themeMode: Int = 0
) {
    var expanded by remember { mutableStateOf(false) }
    val mutedColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    Column {
        if (label != null) {
            Text(label, fontSize = 12.sp, color = mutedColor, modifier = Modifier.padding(bottom = 6.dp))
        }
        Box {
            Row(
                modifier = if (themeMode == 3) {
                    Modifier
                        .fillMaxWidth()
                        .glassyCard(RoundedCornerShape(12.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF374151) else Color(0xFFF9FAFB))
                        .clickable { expanded = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = account?.let { "${it.name} (${it.currency})" } ?: "Оберіть рахунок",
                    fontSize = 16.sp,
                    color = account?.let { textColor } ?: mutedColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = mutedColor)
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = if (themeMode == 3) {
                    Modifier.glassyCard(RoundedCornerShape(12.dp))
                } else {
                    Modifier.background(if(isDark) Color(0xFF1F2937) else Color.White)
                }
            ) {
                accounts.forEach { acc ->
                    DropdownMenuItem(
                        text = { Text("${acc.name} (${acc.currency})", color = textColor) },
                        onClick = {
                            onAccountSelected(acc)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AmountInput(
    amountValue: TextFieldValue,
    onAmountChange: (TextFieldValue) -> Unit,
    currency: String,
    isDark: Boolean,
    onFocusChange: (Boolean) -> Unit,
    hideCurrencyPrefix: Boolean = true,
    themeMode: Int = 0
) {
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val mutedColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        if (!hideCurrencyPrefix) {
            Text(
                text = currency,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = mutedColor,
                modifier = Modifier.padding(bottom = 4.dp, end = 8.dp)
            )
        }
        
        BasicTextField(
            value = amountValue,
            onValueChange = onAmountChange,
            textStyle = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Just clear focus
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state -> onFocusChange(state.isFocused) },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (amountValue.text.isEmpty()) {
                            Text("0", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = mutedColor.copy(alpha = 0.5f))
                        }
                        innerTextField()
                    }
                    
                    if (amountValue.text.any { it in listOf('+', '-', '*', '/', '%') }) {
                        val evaluated = evaluateMath(amountValue.text)
                        if (evaluated != null) {
                            val fmt = if (evaluated == evaluated.toLong().toDouble()) {
                                evaluated.toLong().toString()
                            } else {
                                String.format(Locale.US, "%.2f", evaluated).trimEnd('0').trimEnd('.')
                            }
                            Text(
                                text = "=$fmt",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = mutedColor.copy(alpha = 0.8f),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        )
    }
    val dividerColor = if (themeMode == 3) Color(0x33FFFFFF) else if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
    HorizontalDivider(color = dividerColor, thickness = 2.dp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
fun InlineCalculator(
    onOperatorClick: (String) -> Unit,
    onEqualClick: () -> Unit,
    isDark: Boolean,
    themeMode: Int = 0
) {
    val btnBg = if (themeMode == 3) Color(0x22FFFFFF) else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)
    val btnText = if (isDark) Color.White else Color(0xFF111827)
    val ops = listOf("+", "-", "*", "/", "%")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ops.forEach { op ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(btnBg)
                    .clickable { onOperatorClick(op) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(op, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = btnText)
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF3B82F6)) // primary
                .clickable(onClick = onEqualClick)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("=", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

fun evaluateMath(expression: String): Double? {
    if (expression.isBlank()) return null
    try {
        val str = expression.replace(" ", "").replace(",", ".").replace("×", "*").replace("÷", "/")
        val tokens = ArrayList<String>()
        var currentNumber = ""
        for (char in str) {
            if (char in listOf('+', '-', '*', '/', '%')) {
                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber)
                    currentNumber = ""
                }
                tokens.add(char.toString())
            } else {
                currentNumber += char
            }
        }
        if (currentNumber.isNotEmpty()) {
            tokens.add(currentNumber)
        }

        if (tokens.isEmpty()) return null

        if (tokens.isNotEmpty() && (tokens[0] == "-" || tokens[0] == "+")) {
            tokens.add(0, "0")
        }

        // Pass 1: percentages
        var i = 0
        while (i < tokens.size) {
            if (tokens[i] == "%") {
                if (i > 0) {
                    val left = tokens[i - 1].toDoubleOrNull() ?: 1.0
                    tokens[i - 1] = (left / 100.0).toString()
                    tokens.removeAt(i)
                } else {
                    tokens.removeAt(i)
                }
            } else {
                i++
            }
        }

        // Pass 2: multiplication and division
        i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token == "*" || token == "/") {
                if (i > 0 && i + 1 < tokens.size) {
                    val left = tokens[i - 1].toDoubleOrNull() ?: 0.0
                    val right = tokens[i + 1].toDoubleOrNull() ?: 0.0
                    val res = if (token == "*") left * right else if (right != 0.0) left / right else 0.0
                    tokens[i - 1] = res.toString()
                    tokens.removeAt(i)
                    tokens.removeAt(i)
                } else {
                    i++
                }
            } else {
                i++
            }
        }

        // Pass 3: addition and subtraction
        i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token == "+" || token == "-") {
                if (i > 0 && i + 1 < tokens.size) {
                    val left = tokens[i - 1].toDoubleOrNull() ?: 0.0
                    val right = tokens[i + 1].toDoubleOrNull() ?: 0.0
                    val res = if (token == "+") left + right else left - right
                    tokens[i - 1] = res.toString()
                    tokens.removeAt(i)
                    tokens.removeAt(i)
                } else {
                    i++
                }
            } else {
                i++
            }
        }

        return tokens.firstOrNull()?.toDoubleOrNull()
    } catch (e: Exception) {
        return null
    }
}

