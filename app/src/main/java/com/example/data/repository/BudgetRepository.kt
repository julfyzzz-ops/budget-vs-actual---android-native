package com.example.data.repository

import com.example.data.dao.BudgetDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val dao: BudgetDao) {
    val allAccounts: Flow<List<Account>> = dao.getAllAccounts()
    val allCategories: Flow<List<Category>> = dao.getAllCategories()
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    suspend fun insertAccount(account: Account) = dao.insertAccount(account)
    suspend fun updateAccount(account: Account) = dao.updateAccount(account)
    suspend fun deleteAccount(account: Account) = dao.deleteAccount(account)

    suspend fun insertCategory(category: Category) = dao.insertCategory(category)
    suspend fun updateCategory(category: Category) = dao.updateCategory(category)
    suspend fun deleteCategory(category: Category) = dao.deleteCategory(category)

    suspend fun insertTransaction(transaction: TransactionEntity) = dao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: TransactionEntity) = dao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: TransactionEntity) = dao.deleteTransaction(transaction)

    suspend fun restoreDatabase(backup: DatabaseBackup) = dao.restoreDatabase(backup)
}
