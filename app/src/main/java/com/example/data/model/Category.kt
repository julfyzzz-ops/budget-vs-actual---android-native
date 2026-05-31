package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

@Entity(tableName = "categories")
@JsonClass(generateAdapter = true)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: TransactionType,
    val iconName: String,
    val color: Long,
    val currentLimit: Double = 0.0,
    val limitHistoryJson: String = "{}",
    val orderIndex: Int = 0
) {
    fun getLimitForMonth(month: Int, year: Int): Double {
        val targetKey = String.format("%04d-%02d", year, month + 1)
        return try {
            val json = org.json.JSONObject(limitHistoryJson)
            if (json.length() == 0) return currentLimit
            var applicableLimit = currentLimit
            var latestKey = ""
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key <= targetKey) {
                    if (latestKey == "" || key > latestKey) {
                        latestKey = key
                        applicableLimit = json.getDouble(key)
                    }
                }
            }
            if (latestKey.isNotEmpty()) {
                applicableLimit
            } else {
                currentLimit
            }
        } catch (e: Exception) {
            currentLimit
        }
    }
}
