package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DatabaseBackup(
    val accounts: List<Account>,
    val categories: List<Category>,
    val transactions: List<TransactionEntity>
)
