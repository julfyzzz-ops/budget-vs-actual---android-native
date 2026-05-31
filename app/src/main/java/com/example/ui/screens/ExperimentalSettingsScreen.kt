package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.os.Build
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.TransactionType
import com.example.ui.viewmodels.MainViewModel

@Composable
fun ExperimentalSettingsScreen(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val themeMode by viewModel.themeMode.collectAsState()
    val notificationParserEnabled by viewModel.notificationParserEnabled.collectAsState()
    val notificationAppsString by viewModel.notificationParserApps.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()

    var isPostNotificationsGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var isListenerEnabled by remember {
        mutableStateOf(false)
    }

    fun checkListenerEnabled(ctx: Context): Boolean {
        val pkgName = ctx.packageName
        val flat = android.provider.Settings.Secure.getString(ctx.contentResolver, "enabled_notification_listeners")
        if (flat != null) {
            val names = flat.split(":")
            for (name in names) {
                val cn = android.content.ComponentName.unflattenFromString(name)
                if (cn != null && cn.packageName == pkgName) {
                    return true
                }
            }
        }
        return false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            isPostNotificationsGranted = isGranted
            if (isGranted) {
                Toast.makeText(context, "Дозвіл на сповіщення надано!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Без цього дозволу ви не побачите статус імпорту", Toast.LENGTH_LONG).show()
            }
        }
    )

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isListenerEnabled = checkListenerEnabled(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    isPostNotificationsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        isListenerEnabled = checkListenerEnabled(context)
    }
    val isDark = when(themeMode) {
        1 -> false
        2, 3 -> true
        else -> isSystemDark
    }

    val sheetBg = if (themeMode == 3) Color.Transparent else if (isDark) Color(0xFF1F2937) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val subtleText = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cardBg = if (themeMode == 3) Color(0x22111827) else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)

    var customPackageInput by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var appToEditPackage by remember { mutableStateOf("") }
    var appToEditNewPackage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Редагувати пакет додатку", color = textColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Введіть новий package name для додатку:", color = subtleText, fontSize = 14.sp)
                    OutlinedTextField(
                        value = appToEditNewPackage,
                        onValueChange = { appToEditNewPackage = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedAppsList = notificationAppsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                        val index = selectedAppsList.indexOf(appToEditPackage)
                        if (index != -1 && appToEditNewPackage.isNotBlank()) {
                            selectedAppsList[index] = appToEditNewPackage.trim()
                            viewModel.setNotificationParserApps(selectedAppsList.joinToString(","))
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Зберегти", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Скасувати", color = subtleText)
                }
            },
            containerColor = sheetBg
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(sheetBg)
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFEF4444).copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Імпорт з банківських пушів",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(36.dp)
                    .background(cardBg, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Закрити",
                    tint = subtleText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Налаштування автоматичного імпорту",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = subtleText,
                letterSpacing = 0.5.sp
            )

            // Parsing Feature Toggle Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(
                            text = "Імпорт з банківських пушів",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Автоматичне зчитування SMS та сповіщень",
                            fontSize = 12.sp,
                            color = subtleText
                        )
                    }
                    Switch(
                        checked = notificationParserEnabled,
                        onCheckedChange = { isChecked ->
                            viewModel.setNotificationParserEnabled(isChecked)
                            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPostNotificationsGranted) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFEF4444),
                            uncheckedThumbColor = subtleText,
                            uncheckedTrackColor = cardBg.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = (if (isDark) Color(0xFF4B5563) else Color(0xFFE5E7EB)), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ця функція дозволяє автоматично створювати транзакції на основі тексту з вхідних сповіщень ваших банківських додатків (Mono, Privat24, тощо). Жодні дані не відправляються на сервери, обробка виконується виключно локально за заданими ключовими словами для рахунків та статей витрат.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = subtleText
                )

                if (notificationParserEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.08f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🔬 Статус: Функція активна. Слідуйте крокам нижче для налаштування парсингу.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF87171),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (notificationParserEnabled) {
                // Step 1: Grant Android permission
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Крок 1: Дозволи системи",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Для повноцінної роботи функції потрібні два системні дозволи: читання сповіщень банків та показ звітів про авто-імпорт.",
                        fontSize = 12.sp,
                        color = subtleText,
                        lineHeight = 16.sp
                    )
                    
                    // Permission 1 status
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. Читання сповіщень банків:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isListenerEnabled) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isListenerEnabled) "Надано" else "Не надано",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isListenerEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }

                    // Permission 2 status
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Показ звітів про авто-імпорт:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isPostNotificationsGranted) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isPostNotificationsGranted) "Надано" else "Не надано",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPostNotificationsGranted) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!isListenerEnabled) {
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Не вдалося відкрити налаштування", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("grant_notification_access"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("1. Надати доступ до сповіщень", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPostNotificationsGranted) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("grant_push_access"),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isListenerEnabled) Color(0xFFEF4444) else MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("2. Дозволити показ сповіщень", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Step 2: App Selection checklist
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Крок 2: Налаштування додатків",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Оберіть зі списку популярних або додайте свій власний пакет:",
                        fontSize = 12.sp,
                        color = subtleText,
                        lineHeight = 16.sp
                    )

                    val selectedApps = notificationAppsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                    val predefinedApps = listOf(
                        Pair("Monobank (com.ftband.mono)", "com.ftband.mono"),
                        Pair("Приват24 (ua.privatbank.ap24)", "ua.privatbank.ap24"),
                        Pair("Sense SuperApp (com.alfabank.kiev)", "com.alfabank.kiev"),
                        Pair("Ощадбанк (ua.oschadbank.oschadny24)", "ua.oschadbank.oschadny24"),
                        Pair("VST Bank (com.vostok.bv)", "com.vostok.bv"),
                        Pair("Raif (ua.raiffeisen.myraif)", "ua.raiffeisen.myraif"),
                        Pair("SMS (Google Повідомлення)", "com.google.android.apps.messaging")
                    )

                    // 1. Predefined Checklist section
                    Text(
                        text = "Швидкий вибір шаблонів:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    predefinedApps.forEach { (label, pkg) ->
                        val isChecked = selectedApps.contains(pkg)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    val updated = if (checked) selectedApps + pkg else selectedApps - pkg
                                    viewModel.setNotificationParserApps(updated.joinToString(","))
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444))
                            )
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                color = textColor,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = (if (isDark) Color(0xFF4B5563) else Color(0xFFE5E7EB)), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. Custom App Addition section
                    Text(
                        text = "Додати власний додаток:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = customPackageInput,
                            onValueChange = { customPackageInput = it },
                            placeholder = { Text("Введіть package name, наприклад com.mybank", fontSize = 12.sp, color = subtleText) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = Color(0xFFEF4444),
                                unfocusedBorderColor = if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = {
                                val pkg = customPackageInput.trim()
                                if (pkg.isNotEmpty()) {
                                    if (selectedApps.contains(pkg)) {
                                        Toast.makeText(context, "Цей додаток вже додано!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val updated = selectedApps + pkg
                                        viewModel.setNotificationParserApps(updated.joinToString(","))
                                        customPackageInput = ""
                                        Toast.makeText(context, "Додаток успішно додано", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Введіть коректний package name", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Додати", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = (if (isDark) Color(0xFF4B5563) else Color(0xFFE5E7EB)), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    // 3. Swipeable Active Apps List
                    Text(
                        text = "Ваші активні додатки (${selectedApps.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    
                    if (selectedApps.isEmpty()) {
                        Text(
                            text = "Немає вибраних додатків",
                            fontSize = 12.sp,
                            color = subtleText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    } else {
                        Text(
                            text = "👉 Свайп вліво для видалення 🔴 | вправо для редагування 🔵",
                            fontSize = 11.sp,
                            color = subtleText,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            selectedApps.forEach { pkg ->
                                key(pkg) {
                                    val label = when (pkg) {
                                        "com.ftband.mono" -> "Monobank"
                                        "ua.privatbank.ap24" -> "Приват24"
                                        "com.alfabank.kiev" -> "Sense Bank"
                                        "ua.oschadbank.oschadny24" -> "Ощадбанк"
                                        "com.vostok.bv" -> "VST Bank"
                                        "ua.raiffeisen.myraif" -> "Raiffeisen"
                                        "com.google.android.apps.messaging" -> "SMS (Google)"
                                        else -> pkg
                                    }
                                    
                                    SwipeableAppItem(
                                        packageName = pkg,
                                        label = label,
                                        isDark = isDark,
                                        onDelete = {
                                            val updated = selectedApps - pkg
                                            viewModel.setNotificationParserApps(updated.joinToString(","))
                                            Toast.makeText(context, "Видалено: $label", Toast.LENGTH_SHORT).show()
                                        },
                                        onEdit = { oldPkg ->
                                            appToEditPackage = oldPkg
                                            appToEditNewPackage = oldPkg
                                            showEditDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Step 3: Account Keywords
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Крок 3: Ключові слова для Рахунків",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Введіть слова розділені комами, які зустрічаються у банківських SMS/пушах для ідентифікації кожного рахунку.",
                        fontSize = 12.sp,
                        color = subtleText,
                        lineHeight = 16.sp
                    )

                    accounts.forEach { account ->
                        AccountKeywordsEditor(
                            account = account,
                            viewModel = viewModel,
                            isDark = isDark,
                            textColor = textColor,
                            subtleText = subtleText
                        )
                    }
                }

                // Step 4: Category Keywords
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Крок 4: Ключові слова для Категорій",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Введіть слова-категорії (наприклад назви магазинів), які відповідають статям витрат.",
                        fontSize = 12.sp,
                        color = subtleText,
                        lineHeight = 16.sp
                    )

                    categories.forEach { category ->
                        CategoryKeywordsEditor(
                            category = category,
                            viewModel = viewModel,
                            isDark = isDark,
                            textColor = textColor,
                            subtleText = subtleText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AccountKeywordsEditor(
    account: Account,
    viewModel: MainViewModel,
    isDark: Boolean,
    textColor: Color,
    subtleText: Color
) {
    val keywords by viewModel.getAccountKeywords(account.id).collectAsState("")

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "Рахунок: ${account.name}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
        OutlinedTextField(
            value = keywords,
            onValueChange = { viewModel.setAccountKeywords(account.id, it) },
            placeholder = { Text("моно, monobank, чорна, фоп", fontSize = 12.sp, color = subtleText) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = Color(0xFFEF4444),
                unfocusedBorderColor = if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB)
            )
        )
    }
}

@Composable
fun CategoryKeywordsEditor(
    category: Category,
    viewModel: MainViewModel,
    isDark: Boolean,
    textColor: Color,
    subtleText: Color
) {
    val keywords by viewModel.getCategoryKeywords(category.id).collectAsState("")

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        val typeLabel = if (category.type == TransactionType.INCOME) "Д" else "В"
        val typeColor = if (category.type == TransactionType.INCOME) Color(0xFF10B981) else Color(0xFFEF4444)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = typeLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = typeColor)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
        OutlinedTextField(
            value = keywords,
            onValueChange = { viewModel.setCategoryKeywords(category.id, it) },
            placeholder = { Text("сільпо, атб, metro, ліки, заправка", fontSize = 12.sp, color = subtleText) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = Color(0xFFEF4444),
                unfocusedBorderColor = if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB)
            )
        )
    }
}

@Composable
fun SwipeableAppItem(
    packageName: String,
    label: String,
    isDark: Boolean,
    onDelete: () -> Unit,
    onEdit: (String) -> Unit
) {
    var offsetX by remember(packageName) { mutableStateOf(0f) }
    val density = LocalDensity.current
    val minSwipeDistance = with(density) { 60.dp.toPx() }
    val maxSwipeDistance = with(density) { 100.dp.toPx() }
    
    val animatedOffset by animateFloatAsState(targetValue = offsetX)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) Color(0xFF1F2937) else Color(0xFFE5E7EB))
    ) {
        // Red background on swipe left (delete)
        if (offsetX < 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEF4444))
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Видалити",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        
        // Blue background on swipe right (edit)
        if (offsetX > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF3B82F6))
                    .padding(start = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Редагувати",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        
        // Foreground
        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .background(if (isDark) Color(0xFF374151) else Color.White)
                .pointerInput(packageName) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -minSwipeDistance) {
                                onDelete()
                            } else if (offsetX > minSwipeDistance) {
                                onEdit(packageName)
                            }
                            offsetX = 0f
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-maxSwipeDistance, maxSwipeDistance)
                        }
                    )
                }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )
                Text(
                    text = packageName,
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                )
            }
        }
    }
}

