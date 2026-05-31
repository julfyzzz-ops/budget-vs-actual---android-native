package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BudgetRepository
import com.example.data.settings.SettingsRepository
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(
    val repository: BudgetRepository,
    val settingsRepository: SettingsRepository
) : ViewModel() {
    
    init {
        viewModelScope.launch {
            val accountsList = repository.allAccounts.first()
            if (accountsList.isEmpty()) {
                prepopulateDatabase()
            }
        }
        viewModelScope.launch {
            settingsRepository.numberFormatFlow.collect { format ->
                com.example.ui.utils.CurrencyUtils.isWholeNumbersOnly = (format == 1)
            }
        }
    }
    
    private suspend fun prepopulateDatabase() {
        val uahAccount = Account(name = "Mono біла", initialBalance = 12799.15, currency = "UAH", type = AccountType.CURRENT, color = 0xFFFFFFFF, iconName = "credit_card")
        val usdAccount = Account(name = "Cash USD", initialBalance = 16800.0, currency = "USD", type = AccountType.SAVINGS, color = 0xFF000000, iconName = "wallet", exchangeRate = 40.0)
        repository.insertAccount(uahAccount)
        repository.insertAccount(usdAccount)

        val groceriesCategory = Category(name = "Продукти", type = TransactionType.EXPENSE, color = 0xFF4CAF50, iconName = "shopping_cart")
        val transportCategory = Category(name = "Транспорт", type = TransactionType.EXPENSE, color = 0xFFFF9800, iconName = "directions_bus")
        val salaryCategory = Category(name = "Зарплата", type = TransactionType.INCOME, color = 0xFF4CAF50, iconName = "work")
        repository.insertCategory(groceriesCategory)
        repository.insertCategory(transportCategory)
        repository.insertCategory(salaryCategory)

        // Wait to get IDs
        val accs = repository.allAccounts.first()
        val cats = repository.allCategories.first()
        
        if (accs.size >= 2 && cats.size >= 3) {
            val uahId = accs[0].id
            val usdId = accs[1].id
            val grocId = cats[0].id
            val transId = cats[1].id
            val salId = cats[2].id
            
            val now = Calendar.getInstance().timeInMillis
            repository.insertTransaction(TransactionEntity(amount = 25000.0, currency = "UAH", accountId = uahId, categoryId = salId, type = TransactionType.INCOME, timestamp = now, note = "Аванс"))
            repository.insertTransaction(TransactionEntity(amount = 354.0, currency = "UAH", accountId = uahId, categoryId = grocId, type = TransactionType.EXPENSE, timestamp = now - 86400000, note = "АТБ"))
            repository.insertTransaction(TransactionEntity(amount = 80.0, currency = "UAH", accountId = uahId, categoryId = transId, type = TransactionType.EXPENSE, timestamp = now - 86400000 * 2, note = "Маршрутка"))
        }
    }
    
    val incognitoMode = settingsRepository.incognitoModeFlow.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val themeMode = settingsRepository.themeModeFlow.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val numberFormat = settingsRepository.numberFormatFlow.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val language = settingsRepository.languageFlow.stateIn(viewModelScope, SharingStarted.Lazily, "uk")
    val notificationParserEnabled = settingsRepository.notificationParserEnabledFlow.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val notificationParserApps = settingsRepository.notificationParserAppsFlow.stateIn(viewModelScope, SharingStarted.Lazily, "com.ftband.mono,ua.privatbank.ap24")
    
    val usdRate = settingsRepository.usdRateFlow.stateIn(viewModelScope, SharingStarted.Lazily, 40.0f)
    val eurRate = settingsRepository.eurRateFlow.stateIn(viewModelScope, SharingStarted.Lazily, 42.5f)
    val plnRate = settingsRepository.plnRateFlow.stateIn(viewModelScope, SharingStarted.Lazily, 9.5f)
    val gbpRate = settingsRepository.gbpRateFlow.stateIn(viewModelScope, SharingStarted.Lazily, 50.0f)

    fun getRateForCurrency(currency: String): Double {
        return when (currency) {
            "USD" -> usdRate.value.toDouble()
            "EUR" -> eurRate.value.toDouble()
            "PLN" -> plnRate.value.toDouble()
            "GBP" -> gbpRate.value.toDouble()
            else -> 1.0
        }
    }
    
    fun updateRates(usd: Float, eur: Float, pln: Float, gbp: Float) {
        viewModelScope.launch {
            settingsRepository.setRate("USD", usd)
            settingsRepository.setRate("EUR", eur)
            settingsRepository.setRate("PLN", pln)
            settingsRepository.setRate("GBP", gbp)
            
            // Update rates for all existing accounts
            val allAccounts = accounts.value
            allAccounts.forEach { acc ->
                val newRate = when (acc.currency) {
                    "USD" -> usd.toDouble()
                    "EUR" -> eur.toDouble()
                    "PLN" -> pln.toDouble()
                    "GBP" -> gbp.toDouble()
                    else -> 1.0
                }
                if (acc.exchangeRate != newRate) {
                    repository.updateAccount(acc.copy(exchangeRate = newRate))
                }
            }
        }
    }
    
    val accounts = repository.allAccounts.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val categories = repository.allCategories.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val transactions = repository.allTransactions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Month is 0-indexed like Calendar
    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val currentMonth: StateFlow<Int> = _currentMonth
    
    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear
    
    val isEditMode = MutableStateFlow(false)
    
    private val _filterAccountId = MutableStateFlow<Int?>(null)
    val filterAccountId: StateFlow<Int?> = _filterAccountId
    
    private val _filterCategoryId = MutableStateFlow<Int?>(null)
    val filterCategoryId: StateFlow<Int?> = _filterCategoryId

    fun setFilterAccountId(id: Int?) { _filterAccountId.value = id }
    fun setFilterCategoryId(id: Int?) { _filterCategoryId.value = id }
    
    fun toggleEditMode() {
        isEditMode.value = !isEditMode.value
    }
    
    fun getTransactionsForMonth(month: Int, year: Int): List<TransactionEntity> {
        return transactions.value.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
        }
    }
    
    fun getAccountBalance(accountId: Int): Double {
        val acc = accounts.value.find { it.id == accountId } ?: return 0.0
        var balance = acc.initialBalance
        transactions.value.forEach { tx ->
            if (tx.accountId == accountId) {
                when(tx.type) {
                    TransactionType.INCOME -> balance += tx.amount
                    TransactionType.EXPENSE -> balance -= tx.amount
                    TransactionType.TRANSFER -> balance -= tx.amount
                }
            }
            if (tx.receiverAccountId == accountId && tx.type == TransactionType.TRANSFER) {
                balance += (tx.receiverAmount ?: tx.amount)
            }
        }
        return balance
    }
    
    fun previousMonth() {
        if (_currentMonth.value == 0) {
            _currentMonth.value = 11
            _currentYear.value -= 1
        } else {
            _currentMonth.value -= 1
        }
    }
    
    fun nextMonth() {
        if (_currentMonth.value == 11) {
            _currentMonth.value = 0
            _currentYear.value += 1
        } else {
            _currentMonth.value += 1
        }
    }
    
    fun toggleIncognitoMode() {
        viewModelScope.launch {
            settingsRepository.setIncognitoMode(!incognitoMode.value)
        }
    }
    
    fun setIncognitoMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setIncognitoMode(enabled)
        }
    }
    
    fun setNumberFormat(format: Int) {
        viewModelScope.launch {
            settingsRepository.setNumberFormat(format)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(lang)
        }
    }
    
    private val _draftTransactionFlow = MutableStateFlow<DraftTransaction?>(null)
    val draftTransactionFlow: StateFlow<DraftTransaction?> = _draftTransactionFlow.asStateFlow()

    fun setDraftTransaction(draft: DraftTransaction?) {
        _draftTransactionFlow.value = draft
    }
    
    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setNotificationParserEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationParserEnabled(enabled)
        }
    }

    fun setNotificationParserApps(apps: String) {
        viewModelScope.launch {
            settingsRepository.setNotificationParserApps(apps)
        }
    }

    fun getAccountKeywords(accountId: Int): Flow<String> {
        return settingsRepository.getAccountKeywordsFlow(accountId)
    }

    fun setAccountKeywords(accountId: Int, keywords: String) {
        viewModelScope.launch {
            settingsRepository.setAccountKeywords(accountId, keywords)
        }
    }

    fun getCategoryKeywords(categoryId: Int): Flow<String> {
        return settingsRepository.getCategoryKeywordsFlow(categoryId)
    }

    fun setCategoryKeywords(categoryId: Int, keywords: String) {
        viewModelScope.launch {
            settingsRepository.setCategoryKeywords(categoryId, keywords)
        }
    }
    
    // Add helpers for CRUD operations
    fun addAccount(account: Account) { viewModelScope.launch { repository.insertAccount(account) } }
    fun updateAccount(account: Account) { viewModelScope.launch { repository.updateAccount(account) } }
    fun deleteAccount(account: Account) { viewModelScope.launch { repository.deleteAccount(account) } }

    fun addCategory(category: Category) { viewModelScope.launch { repository.insertCategory(category) } }
    fun updateCategory(category: Category) { viewModelScope.launch { repository.updateCategory(category) } }
    fun deleteCategory(category: Category) { viewModelScope.launch { repository.deleteCategory(category) } }
    
    fun addTransaction(transaction: TransactionEntity) { viewModelScope.launch { repository.insertTransaction(transaction) } }
    fun deleteTransaction(transaction: TransactionEntity) { viewModelScope.launch { repository.deleteTransaction(transaction) } }

    suspend fun exportDatabase(): String {
        val backup = DatabaseBackup(
            accounts = repository.allAccounts.first(),
            categories = repository.allCategories.first(),
            transactions = repository.allTransactions.first()
        )
        val moshi = com.squareup.moshi.Moshi.Builder().build()
        val adapter = moshi.adapter(DatabaseBackup::class.java)
        return adapter.toJson(backup)
    }

    fun importDatabase(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val moshi = com.squareup.moshi.Moshi.Builder().build()
                val adapter = moshi.adapter(DatabaseBackup::class.java)
                var backup = try { adapter.fromJson(json) } catch (e: Exception) { null }
                
                if (backup == null) {
                    backup = com.example.utils.LegacyDatabaseParser.parse(json)
                }

                if (backup != null) {
                    repository.restoreDatabase(backup)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }
}

class MainViewModelFactory(
    private val repository: BudgetRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class DraftTransaction(
    val amount: Double,
    val accountId: Int?,
    val categoryId: Int?,
    val type: TransactionType,
    val note: String
)

