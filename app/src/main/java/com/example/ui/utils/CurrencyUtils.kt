package com.example.ui.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    private val EMOJIS = arrayOf("🙈", "🙉", "🙊", "👀", "😸", "😹", "😺", "😻", "😼", "😽")
    
    @Volatile
    var isWholeNumbersOnly: Boolean = false
    
    fun formatAmount(amount: Double, currencyCode: String = "UAH", incognito: Boolean = false): String {
        val displayCurrency = if (currencyCode.equals("грн", ignoreCase = true)) "UAH" else currencyCode
        val suffix = if (displayCurrency.isNotBlank()) " $displayCurrency" else ""

        if (incognito) {
            val amountStr = Math.abs(amount).toInt().toString()
            val emojiStr = amountStr.map { char ->
                if (char.isDigit()) EMOJIS[char.digitToInt()] else char.toString()
            }.joinToString("")
            return "$emojiStr$suffix".trim()
        }

        val symbols = try {
            DecimalFormatSymbols.getInstance(Locale.getDefault())
        } catch (e: Exception) {
            DecimalFormatSymbols(Locale.US)
        }
        
        symbols.groupingSeparator = ' '
        symbols.decimalSeparator = ','

        val pattern = if (isWholeNumbersOnly) "#,##0" else "#,##0.00"
        val format = DecimalFormat(pattern, symbols)
        val formattedNumber = format.format(amount)

        return "$formattedNumber$suffix".trim()
    }
}
