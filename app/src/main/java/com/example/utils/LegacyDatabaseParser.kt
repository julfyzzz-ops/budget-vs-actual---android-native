package com.example.utils

import com.example.data.model.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object LegacyDatabaseParser {

    fun parse(jsonString: String): DatabaseBackup? {
        try {
            val root = JSONObject(jsonString)
            val accountsArray = root.optJSONArray("accounts") ?: return null
            val categoriesArray = root.optJSONArray("categories") ?: return null
            val transactionsArray = root.optJSONArray("transactions") ?: return null

            val accountIdMap = mutableMapOf<String, Int>()
            val categoryIdMap = mutableMapOf<String, Int>()

            val parsedAccounts = mutableListOf<Account>()
            val parsedCategories = mutableListOf<Category>()
            val parsedTransactions = mutableListOf<TransactionEntity>()

            // We need to generate integer IDs
            var currentAccountId = 1
            var currentCategoryId = 1

            for (i in 0 until accountsArray.length()) {
                val obj = accountsArray.getJSONObject(i)
                val strId = obj.optString("id")
                val intId = currentAccountId++
                if (strId.isNotEmpty()) {
                    accountIdMap[strId] = intId
                }

                val colorHex = obj.optString("color", "#000000")
                val colorLong = try {
                    android.graphics.Color.parseColor(colorHex).toLong() and 0xFFFFFFFFL
                } catch (e: Exception) {
                    0xFF000000L
                }

                val typeStr = obj.optString("type", "CURRENT")
                val type = try { AccountType.valueOf(typeStr) } catch(e:Exception) { AccountType.CURRENT }

                parsedAccounts.add(
                    Account(
                        id = intId,
                        name = obj.optString("name", ""),
                        currency = obj.optString("currency", "UAH"),
                        initialBalance = obj.optDouble("initialBalance", 0.0),
                        color = colorLong,
                        iconName = obj.optString("icon", "wallet"),
                        type = type,
                        exchangeRate = obj.optDouble("currentRate", 1.0),
                        isHidden = obj.optBoolean("isHidden", false),
                        creditLimit = 0.0,
                        orderIndex = i
                    )
                )
            }

            for (i in 0 until categoriesArray.length()) {
                val obj = categoriesArray.getJSONObject(i)
                val strId = obj.optString("id")
                val intId = currentCategoryId++
                if (strId.isNotEmpty()) {
                    categoryIdMap[strId] = intId
                }

                val colorHex = obj.optString("color", "#000000")
                val colorLong = try {
                    android.graphics.Color.parseColor(colorHex).toLong() and 0xFFFFFFFFL
                } catch (e: Exception) {
                    0xFF000000L
                }

                val typeStr = obj.optString("type", "EXPENSE")
                val type = try { TransactionType.valueOf(typeStr) } catch(e:Exception) { TransactionType.EXPENSE }

                val budgetHistoryJson = obj.optJSONObject("budgetHistory")?.toString() ?: "{}"

                parsedCategories.add(
                    Category(
                        id = intId,
                        name = obj.optString("name", ""),
                        type = type,
                        iconName = obj.optString("icon", "more-horizontal"),
                        color = colorLong,
                        currentLimit = obj.optDouble("monthlyBudget", 0.0),
                        limitHistoryJson = budgetHistoryJson,
                        orderIndex = i
                    )
                )
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            for (i in 0 until transactionsArray.length()) {
                val obj = transactionsArray.getJSONObject(i)
                val dateStr = obj.optString("date")
                val timestamp = try { dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
                
                val typeStr = obj.optString("type", "EXPENSE")
                val type = try { TransactionType.valueOf(typeStr) } catch(e:Exception) { TransactionType.EXPENSE }

                val accountStrId = obj.optString("accountId")
                val accountId = accountIdMap[accountStrId] ?: continue // skip invalid tx

                val categoryStrId = obj.optString("categoryId")
                // in transfers, categoryId might be "transfer", we should map it to null or handle differently
                val categoryId = categoryIdMap[categoryStrId]

                val toAccountStrId = obj.optString("toAccountId")
                val toAccountId = accountIdMap[toAccountStrId]
                
                val rawAmount = obj.optDouble("amount", 0.0)
                val finalAmount = rawAmount

                val senderAcc = parsedAccounts.find { it.id == accountId }
                val receiverAcc = parsedAccounts.find { it.id == toAccountId }
                
                var receiverAmt: Double? = if (obj.has("toAmount")) Math.abs(obj.optDouble("toAmount", 0.0)) else null
                
                if (receiverAmt == null && type == TransactionType.TRANSFER && senderAcc != null && receiverAcc != null && senderAcc.currency != receiverAcc.currency) {
                    val senderRate = senderAcc.exchangeRate.takeIf { it > 0 } ?: 1.0
                    val receiverRate = receiverAcc.exchangeRate.takeIf { it > 0 } ?: 1.0
                    receiverAmt = finalAmount * (senderRate / receiverRate)
                }

                parsedTransactions.add(
                    TransactionEntity(
                        id = 0, // Let room generate it
                        timestamp = timestamp,
                        amount = finalAmount,
                        currency = obj.optString("currency", "UAH"),
                        exchangeRate = obj.optDouble("exchangeRate", 1.0),
                        accountId = accountId,
                        receiverAccountId = toAccountId,
                        receiverAmount = receiverAmt,
                        categoryId = categoryId,
                        note = obj.optString("note", ""),
                        type = type
                    )
                )
            }

            return DatabaseBackup(parsedAccounts, parsedCategories, parsedTransactions)

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
