package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class AccountType {
    CURRENT, DEBT, SAVINGS
}

@Entity(tableName = "accounts")
@JsonClass(generateAdapter = true)
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val currency: String,
    val initialBalance: Double,
    val color: Long,
    val iconName: String,
    val type: AccountType,
    val exchangeRate: Double = 1.0,
    val isHidden: Boolean = false,
    val creditLimit: Double = 0.0,
    val orderIndex: Int = 0
)
