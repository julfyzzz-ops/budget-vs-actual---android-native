package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun SettingsScreen(viewModel: MainViewModel, isExperimental: Boolean = false, onDismiss: () -> Unit) {
    val incognitoMode by viewModel.incognitoMode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val context = LocalContext.current
    
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when(themeMode) {
        1 -> false
        2 -> true
        3 -> isSystemDark
        else -> isSystemDark
    }

    val sheetBg = if (themeMode == 3) Color.Transparent else if (isDark) Color(0xFF1F2937) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val subtleText = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Курси", "Вигляд", "Експорт", "Імпорт")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(sheetBg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isExperimental) "Експериментальні налаштування" else "Налаштування",
                fontSize = if (isExperimental) 18.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(36.dp)
                    .background(if (themeMode == 3) Color(0x33FFFFFF) else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6), CircleShape)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Закрити", tint = subtleText, modifier = Modifier.size(20.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom Tabs
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isActive = selectedTab == index
                val bgColor = if (isActive) (if (themeMode == 3) Color(0x33FFFFFF) else if (isDark) Color(0xFF374151) else Color.White) else Color.Transparent
                val tabTextColor = if (isActive) Color(0xFF10B981) else subtleText
                val borderModifier = if (isActive) Modifier.background(bgColor, RoundedCornerShape(16.dp)).border(1.dp, if(themeMode == 3) Color(0x44FFFFFF) else if(isDark) Color(0xFF4B5563) else Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                     else Modifier.background(bgColor, RoundedCornerShape(16.dp))
                
                Box(
                    modifier = borderModifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedTab = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(title, color = tabTextColor, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            when (selectedTab) {
                0 -> RatesTabNew(viewModel, isDark, textColor, subtleText)
                1 -> AppearanceTabNew(themeMode, incognitoMode, viewModel, isDark, textColor, subtleText)
                2 -> ExportTabNew(viewModel, context, isDark, textColor, subtleText)
                3 -> ImportTabNew(viewModel, context, isDark, textColor, subtleText)
            }
        }
    }
}

@Composable
fun RatesTabNew(viewModel: MainViewModel, isDark: Boolean, textColor: Color, subtleText: Color) {
    val usd by viewModel.usdRate.collectAsState()
    val eur by viewModel.eurRate.collectAsState()
    val pln by viewModel.plnRate.collectAsState()
    val gbp by viewModel.gbpRate.collectAsState()

    var usdRate by remember(usd) { mutableStateOf(usd.toString()) }
    var eurRate by remember(eur) { mutableStateOf(eur.toString()) }
    var plnRate by remember(pln) { mutableStateOf(pln.toString()) }
    var gbpRate by remember(gbp) { mutableStateOf(gbp.toString()) }

    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    
    val inputBg = if (themeMode == 3) Color(0x22FFFFFF) else if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = if (themeMode == 3) Color(0x33FFFFFF) else Color.Transparent,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedContainerColor = inputBg,
        focusedContainerColor = inputBg,
        focusedTextColor = textColor,
        unfocusedTextColor = textColor
    )

    Column {
        Text("Курси валют використовуються для коректного відображення переказів між рахунками в різних валютах, та відображення загального балансу", fontSize = 14.sp, color = subtleText)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(value = usdRate, onValueChange = { usdRate = it }, label = { Text("Курс USD до UAH", color = subtleText) }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = eurRate, onValueChange = { eurRate = it }, label = { Text("Курс EUR до UAH", color = subtleText) }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = plnRate, onValueChange = { plnRate = it }, label = { Text("Курс PLN до UAH", color = subtleText) }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = gbpRate, onValueChange = { gbpRate = it }, label = { Text("Курс GBP до UAH", color = subtleText) }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, shape = RoundedCornerShape(12.dp))
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { 
                val u = usdRate.toFloatOrNull() ?: 40.0f
                val e = eurRate.toFloatOrNull() ?: 42.5f
                val p = plnRate.toFloatOrNull() ?: 9.5f
                val g = gbpRate.toFloatOrNull() ?: 50.0f
                viewModel.updateRates(u, e, p, g)
                Toast.makeText(context, "Курси збережено", Toast.LENGTH_SHORT).show()
            }, 
            modifier = Modifier.fillMaxWidth().height(48.dp), 
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Зберегти курси", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AppearanceTabNew(themeMode: Int, incognitoMode: Boolean, viewModel: MainViewModel, isDark: Boolean, textColor: Color, subtleText: Color) {
    val borderColor = if (themeMode == 3) Color(0x33FFFFFF) else if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
    val inputBg = if (themeMode == 3) Color(0x22FFFFFF) else if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)
    val activeBorder = Color(0xFF10B981)
    val activeBg = if (themeMode == 3) Color(0x3310B981) else if (isDark) Color(0xFF065F46) else Color(0xFFD1FAE5) // green highlight
    val activeText = Color(0xFF10B981)
    
    val numberFormat by viewModel.numberFormat.collectAsState()
    val currentLanguage by viewModel.language.collectAsState()
    
    Column {
        // Theme
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Palette, contentDescription = null, tint = subtleText, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Тема додатку", fontSize = 14.sp, color = subtleText, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(1 to "Світла", 2 to "Темна", 3 to "Android 17").forEach { (v, label) ->
                val isActive = themeMode == v
                val bg = if (isActive) activeBg else inputBg
                val brd = if (isActive) activeBorder else borderColor
                val txt = if (isActive) activeText else subtleText
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bg)
                        .border(2.dp, brd, RoundedCornerShape(16.dp))
                        .clickable { viewModel.setThemeMode(v) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val icon = when (v) {
                            1 -> Icons.Filled.WbSunny
                            2 -> Icons.Filled.DarkMode
                            else -> Icons.Filled.AutoAwesome
                        }
                        Icon(icon, contentDescription = null, tint = txt, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(label, color = txt, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Number Format
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Numbers, contentDescription = null, tint = subtleText, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Формат чисел", fontSize = 14.sp, color = subtleText, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val formats = listOf(
                Triple("Цілі числа", "123", 1),
                Triple("З копійками", ".00", 0),
                Triple("Приховано", "🙈", 2)
            )
            
            formats.forEach { (label, iconText, formatValue) ->
                val isActive = numberFormat == formatValue
                
                val bg = if (isActive) activeBg else inputBg
                val brd = if (isActive) activeBorder else Color.Transparent
                val txt = if (isActive) activeText else textColor
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .border(if (isActive) 2.dp else 0.dp, brd, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setNumberFormat(formatValue) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).background(if(isDark) Color(0xFF374151) else Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(iconText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = subtleText)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(label, color = txt, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    if (isActive) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = activeText, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Language
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Language, contentDescription = null, tint = subtleText, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Мова", fontSize = 14.sp, color = subtleText, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf("uk" to "Українська", "en" to "English").forEach { (code, label) ->
                val isActive = currentLanguage == code
                val bg = if (isActive) activeBg else inputBg
                val brd = if (isActive) activeBorder else borderColor
                val txt = if (isActive) activeText else subtleText
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .border(2.dp, brd, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setLanguage(code) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = txt, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ExportTabNew(viewModel: MainViewModel, context: Context, isDark: Boolean, textColor: Color, subtleText: Color) {
    val coroutineScope = rememberCoroutineScope()
    var jsonText by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        jsonText = viewModel.exportDatabase()
    }
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(jsonText.toByteArray())
                }
                Toast.makeText(context, "Експортовано", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    val themeMode by viewModel.themeMode.collectAsState()
    val inputBg = if (themeMode == 3) Color(0x22FFFFFF) else if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)

    Column {
        Text("Експорт зберігає всі ваші рахунки, транзакції, категорії та налаштування в єдиний текстовий файл формату JSON.", fontSize = 14.sp, color = subtleText)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { exportLauncher.launch("budget_backup.json") }, 
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (themeMode == 3) Color(0x33FFFFFF) else if(isDark) Color(0xFF374151) else Color(0xFFE5E7EB), contentColor = textColor)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Завантажити", fontWeight = FontWeight.SemiBold)
            }
            
            Button(
                onClick = { 
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, jsonText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }, 
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (themeMode == 3) Color(0x33FFFFFF) else if(isDark) Color(0xFF374151) else Color(0xFFE5E7EB), contentColor = textColor)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Поділитися", fontWeight = FontWeight.SemiBold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        var copied by remember { mutableStateOf(false) }
        LaunchedEffect(copied) {
            if (copied) {
                delay(2000)
                copied = false
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(inputBg, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = jsonText,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = subtleText,
                modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState())
            )
            
            FloatingActionButton(
                onClick = { 
                    val clip = ClipData.newPlainText("JSON Backup", jsonText)
                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                    copied = true
                },
                modifier = Modifier.align(Alignment.TopEnd).size(36.dp),
                containerColor = if (themeMode == 3) Color(0x33FFFFFF) else if(isDark) Color(0xFF374151) else Color(0xFFE5E7EB),
                contentColor = textColor,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                if (copied) {
                    Icon(Icons.Filled.Check, contentDescription = "Скопійовано", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                } else {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Копіювати", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ImportTabNew(viewModel: MainViewModel, context: Context, isDark: Boolean, textColor: Color, subtleText: Color) {
    var jsonInput by remember { mutableStateOf("") }
    
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            val stringBuilder = java.lang.StringBuilder()
            try {
                context.contentResolver.openInputStream(it)?.use { input ->
                    BufferedReader(InputStreamReader(input)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            stringBuilder.append(line)
                            line = reader.readLine()
                        }
                    }
                }
                jsonInput = stringBuilder.toString()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    val themeMode by viewModel.themeMode.collectAsState()
    val inputBg = if (themeMode == 3) Color(0x22FFFFFF) else if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)
    val borderColor = if (themeMode == 3) Color(0x44FFFFFF) else if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB)

    Column {
        // Warning Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Увага! Відновлення файлу призведе до перезапису всіх ваших поточних даних.", fontSize = 12.sp, color = Color(0xFF92400E), fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Upload Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .drawBehind {
                    drawRoundRect(
                        color = borderColor,
                        style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
                .clickable { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.UploadFile, contentDescription = null, tint = subtleText, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Обрати файл", color = subtleText, fontWeight = FontWeight.Medium)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = borderColor)
            Text("АБО ВСТАВТЕ ТЕКСТ ТУТ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subtleText, modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = borderColor)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        var hasError by remember { mutableStateOf(false) }
        
        OutlinedTextField(
            value = jsonInput,
            onValueChange = { 
                jsonInput = it
                hasError = false
            },
            placeholder = { Text("Вставте JSON...", color = subtleText) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = textColor),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = inputBg,
                focusedContainerColor = inputBg,
            )
        )
        if (hasError) {
            Text("Невірний формат JSON. Спробуйте ще раз.", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        var showConfirmDialog by remember { mutableStateOf(false) }
        
        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = jsonInput.isNotBlank()
        ) {
            Text("Відновити", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Підтвердження") },
                text = { Text("Ви впевнені, що хочете замінити всі свої дані? Цю дію неможливо скасувати.") },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        viewModel.importDatabase(jsonInput) { success ->
                            if (success) {
                                Toast.makeText(context, "Дані успішно відновлено", Toast.LENGTH_SHORT).show()
                            } else {
                                hasError = true
                            }
                        }
                    }) {
                        Text("Так, відновити", color = Color(0xFF10B981))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Скасувати", color = subtleText)
                    }
                }
            )
        }
    }
}

