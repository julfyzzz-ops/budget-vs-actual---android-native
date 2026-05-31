package com.example.data.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType) = value.name

    @TypeConverter
    fun toTransactionType(value: String) = enumValueOf<TransactionType>(value)

    @TypeConverter
    fun fromAccountType(value: AccountType) = value.name

    @TypeConverter
    fun toAccountType(value: String) = enumValueOf<AccountType>(value)
}
