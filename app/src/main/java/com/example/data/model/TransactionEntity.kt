package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "transactions")
@JsonClass(generateAdapter = true)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val amount: Double, // absolute amount
    val currency: String,
    val exchangeRate: Double = 1.0,
    val accountId: Int,
    val receiverAccountId: Int? = null,
    val receiverAmount: Double? = null,
    val categoryId: Int? = null,
    val note: String = "",
    val type: TransactionType
)
