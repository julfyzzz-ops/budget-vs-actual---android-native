package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.BudgetApplication
import com.example.MainActivity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BankNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val app = application as? BudgetApplication ?: return
        val settingsRepo = app.settingsRepository
        val repository = app.repository

        scope.launch {
            try {
                // 1. Is the notification parser feature toggled on globally?
                val isFeatureEnabled = settingsRepo.notificationParserEnabledFlow.first()
                if (!isFeatureEnabled) return@launch

                // 2. Is this package name in the user's selected applications list?
                val packageName = sbn.packageName ?: ""
                val selectedAppsString = settingsRepo.notificationParserAppsFlow.first()
                val selectedApps = selectedAppsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                
                if (!selectedApps.contains(packageName)) {
                    Log.d("NotificationListener", "Skipping non-configured package: $packageName")
                    return@launch
                }

                // 3. Extract text details safely from SMS or push
                val extras = sbn.notification.extras
                val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
                val text = extras.getString(Notification.EXTRA_TEXT) ?: ""
                val fullText = "$title $text"

                val appName = try {
                    val pm = packageManager
                    val info = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(info).toString()
                } catch (e: Exception) {
                    when (packageName) {
                        "com.ftband.mono" -> "Monobank"
                        "ua.privatbank.ap24" -> "Приват24"
                        "com.alfabank.kiev" -> "Sense Bank"
                        "ua.oschadbank.oschadny24" -> "Ощадбанк"
                        "com.vostok.bv" -> "VST Bank"
                        "ua.raiffeisen.myraif" -> "Raiffeisen"
                        "com.google.android.apps.messaging" -> "SMS"
                        else -> packageName
                    }
                }

                // Show an initial Toast to confirm the app intercepted the push!
                handler.post {
                    Toast.makeText(
                        applicationContext,
                        "Отримано пуш від $appName. Аналізуємо...",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Log.d("NotificationListener", "Processing notification from $packageName ($appName): text = $fullText")

                // 4. Extract amount and type using regex
                val parsedResult = extractAmountAndType(fullText)
                if (parsedResult == null) {
                    Log.w("NotificationListener", "Interception success, but regex parsing failed for: $fullText")
                    // Show a debug notification so the user knows the app intercepted it but didn't find the amount
                    showParsingFailedDebugNotification(applicationContext, appName, fullText)
                    return@launch
                }
                val amount = parsedResult.first
                val type = parsedResult.second

                // 5. Query all matching objects in database to resolve Account & Category
                val accounts = repository.allAccounts.first()
                val categories = repository.allCategories.first()

                if (accounts.isEmpty()) {
                    Log.w("NotificationListener", "Cannot auto-import: No accounts in database")
                    return@launch
                }

                // 5a. Match Account via name or custom keywords
                val matchedAccount = accounts.firstOrNull { account ->
                    val accKeywordsString = settingsRepo.getAccountKeywordsFlow(account.id).first()
                    val keywords = accKeywordsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    keywords.any { fullText.contains(it, ignoreCase = true) } || fullText.contains(account.name, ignoreCase = true)
                }

                // Package and Name Fallback matching for Account if no explicit keyword matched
                var finalAccount = matchedAccount
                if (finalAccount == null) {
                    val lowerText = fullText.lowercase()
                    if (packageName == "com.ftband.mono") {
                        finalAccount = accounts.firstOrNull { 
                            val nameLower = it.name.lowercase()
                            nameLower.contains("mono") || nameLower.contains("моно") || nameLower.contains("black") || nameLower.contains("чорна") || nameLower.contains("біла")
                        } ?: accounts.firstOrNull()
                    } else if (packageName == "ua.privatbank.ap24") {
                        finalAccount = accounts.firstOrNull {
                            val nameLower = it.name.lowercase()
                            nameLower.contains("приват") || nameLower.contains("privat") || nameLower.contains("універсальн")
                        } ?: accounts.firstOrNull()
                    } else if (packageName == "com.alfabank.kiev") {
                        finalAccount = accounts.firstOrNull {
                            val nameLower = it.name.lowercase()
                            nameLower.contains("sense") || nameLower.contains("сенс") || nameLower.contains("альфа")
                        } ?: accounts.firstOrNull()
                    } else {
                        if (accounts.size == 1) {
                            finalAccount = accounts.first()
                        }
                    }
                }

                // 5b. Match Category via name or custom keywords
                val matchedCategory = categories.filter { it.type == type }.firstOrNull { cat ->
                    val catKeywordsString = settingsRepo.getCategoryKeywordsFlow(cat.id).first()
                    val keywords = catKeywordsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    keywords.any { fullText.contains(it, ignoreCase = true) } || fullText.contains(cat.name, ignoreCase = true)
                }

                // Synonym fallback matching for Category if no explicit keyword matched
                var finalCategory = matchedCategory
                if (finalCategory == null && type == TransactionType.EXPENSE) {
                    val lowerText = fullText.lowercase()
                    val groceriesSynonyms = listOf(
                        "сільпо", "атб", "metro", "фора", "ашан", "novus", "копійка", "таврія", "клас", 
                        "торба", "продуктовий", "супермаркет", "groceries", "market", "магазин", "produkty"
                    )
                    val transportSynonyms = listOf(
                        "таксі", "taxi", "uber", "bolt", "uclon", "автобус", "метро", "заправка", "wog24",
                        "wog", "okko", "socar", "брсм", "пальне", "бензин", "shell", "amich", "транспорт", "bus"
                    )
                    
                    if (groceriesSynonyms.any { lowerText.contains(it) }) {
                        finalCategory = categories.filter { it.type == TransactionType.EXPENSE }.firstOrNull { 
                            val name = it.name.lowercase()
                            name.contains("продукт") || name.contains("їжа") || name.contains("супермаркет")
                        }
                    } else if (transportSynonyms.any { lowerText.contains(it) }) {
                        finalCategory = categories.filter { it.type == TransactionType.EXPENSE }.firstOrNull {
                            val name = it.name.lowercase()
                            name.contains("транспорт") || name.contains("авто") || name.contains("таксі") || name.contains("дорога")
                        }
                    }
                } else if (finalCategory == null && type == TransactionType.INCOME) {
                    val lowerText = fullText.lowercase()
                    val salarySynonyms = listOf("зарплата", "salary", "аванс", "надходження", "дохід", "зарахування")
                    if (salarySynonyms.any { lowerText.contains(it) }) {
                        finalCategory = categories.filter { it.type == TransactionType.INCOME }.firstOrNull {
                            val name = it.name.lowercase()
                            name.contains("зарплат") || name.contains("дохід") || name.contains("робота")
                        }
                    }
                }

                val unrecognized = mutableListOf<String>()
                if (finalAccount == null) unrecognized.add("рахунок")
                if (finalCategory == null) unrecognized.add("категорію")

                if (unrecognized.isEmpty() && finalAccount != null && finalCategory != null) {
                    // Success: matched everything!
                    // 6. Construct and insert TransactionEntity
                    val transaction = TransactionEntity(
                        timestamp = System.currentTimeMillis(),
                        amount = amount,
                        currency = finalAccount.currency,
                        exchangeRate = finalAccount.exchangeRate,
                        accountId = finalAccount.id,
                        categoryId = finalCategory.id,
                        note = "Авто-імпорт ($appName)",
                        type = type
                    )

                    repository.insertTransaction(transaction)
                    Log.i("NotificationListener", "Auto-imported transaction: $amount ${finalAccount.currency} for category ${finalCategory.name} using account ${finalAccount.name}")

                    // Create open intent for success clicked
                    val successIntent = Intent(applicationContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        applicationContext,
                        System.currentTimeMillis().toInt(),
                        successIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    showNotification(
                        context = applicationContext,
                        isSuccess = true,
                        appName = appName,
                        amount = amount,
                        unrecognizedFields = emptyList(),
                        pendingIntent = pendingIntent
                    )

                    // Toast success in UI thread for feedback
                    handler.post {
                        Toast.makeText(
                            applicationContext,
                            "Імпортовано транзакцію: ${String.format("%.2f", amount)} грн на рахунок '${finalAccount.name}'",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // Failed: some fields are missing. Do not add transaction. Show actionable notification instead!
                    Log.w("NotificationListener", "Skipping automatic import because some fields are unrecognized: $unrecognized")

                    val clickableIntent = Intent(applicationContext, MainActivity::class.java).apply {
                        putExtra("open_add_transaction", true)
                        putExtra("prepopulate_amount", amount)
                        if (finalAccount != null) {
                            putExtra("prepopulate_account_id", finalAccount.id)
                        }
                        if (finalCategory != null) {
                            putExtra("prepopulate_category_id", finalCategory.id)
                        }
                        putExtra("prepopulate_type", type.name)
                        putExtra("prepopulate_note", "Авто-імпорт ($appName)")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        applicationContext,
                        System.currentTimeMillis().toInt(),
                        clickableIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    showNotification(
                        context = applicationContext,
                        isSuccess = false,
                        appName = appName,
                        amount = amount,
                        unrecognizedFields = unrecognized,
                        pendingIntent = pendingIntent
                    )
                }
            } catch (e: Exception) {
                Log.e("NotificationListener", "Error parsing notification", e)
            }
        }
    }

    private fun showNotification(
        context: Context,
        isSuccess: Boolean,
        appName: String,
        amount: Double,
        unrecognizedFields: List<String>,
        pendingIntent: PendingIntent? = null
    ) {
        val channelId = "bank_imports_high"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Бюджетний автоматичний імпорт",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Термінові сповіщення про успішний та незавершений авто-імпорт транзакцій"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val title = if (isSuccess) "Транзакція розпізнана" else "Не вдалося розпізнати транзакцію"
        val message = if (isSuccess) {
            "Додано: ${String.format("%.2f", amount)} грн"
        } else {
            "По транзакції від $appName не розпізнано: ${unrecognizedFields.joinToString(", ")}. Транзакцію не додано."
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun showParsingFailedDebugNotification(context: Context, appName: String, text: String) {
        val channelId = "bank_imports_high"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Бюджетний автоматичний імпорт",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Термінові сповіщення про успішний та незавершений авто-імпорт транзакцій"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val title = "Пуш отримано: суму не розпізнано"
        val message = "Отримано сповіщення від $appName, але не вдалося розпізнати суму чи валюту. Текст: \"$text\""

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)

        notificationManager.notify((System.currentTimeMillis().toInt() + 1), builder.build())
    }

    private fun extractAmountAndType(text: String): Pair<Double, TransactionType>? {
        // Regex to capture amount with optional sign and thousand separators (spaces, non-breaking spaces \u00A0, thin spaces \u2009)
        val amountRegex = Regex("""(?i)([-+]?\s*\d{1,3}(?:(?:[\s\u00A0\u2009]?\d{3})+)?(?:[\.,]\d{1,2})?)\s*[\s\u00A0\u2009]*(грн|uah|usd|eur|₴|\$|€)""")
        val match = amountRegex.find(text)
        if (match != null) {
            val amountRaw = match.groupValues[1]
            // Clean thousand separators and spaces, keep decimal points
            val cleanedAmountStr = amountRaw
                .replace(Regex("""[\s\u00A0\u2009]"""), "")
                .replace(',', '.')
            
            val rawDouble = cleanedAmountStr.toDoubleOrNull() ?: return null
            val amount = kotlin.math.abs(rawDouble)

            val expenseWords = listOf(
                "списано", "списання", "купівля", "переказ", "витрата", "оплата", 
                "spent", "purchase", "paid", "withdrawal", "комісія", "зняття", 
                "платний", "витрати", "оплачено"
            )
            val incomeWords = listOf(
                "зарахування", "поповнення", "отримано", "received", "refunded", 
                "deposit", "вхідний", "дохід", "зарплата", "надходження"
            )

            val textLower = text.lowercase()
            var type = TransactionType.EXPENSE

            if (incomeWords.any { textLower.contains(it) }) {
                type = TransactionType.INCOME
            } else if (expenseWords.any { textLower.contains(it) }) {
                type = TransactionType.EXPENSE
            } else if (rawDouble < 0) {
                // If amount has a negative sign prefix but no specific words, default to Expense
                type = TransactionType.EXPENSE
            } else if (rawDouble > 0 && amountRaw.trim().startsWith("+")) {
                // If starting with "+", default to Income
                type = TransactionType.INCOME
            }

            return Pair(amount, type)
        }
        return null
    }
}
