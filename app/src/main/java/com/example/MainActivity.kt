package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.data.model.TransactionType
import com.example.ui.BudgetApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.DraftTransaction
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.MainViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as BudgetApplication
        val factory = MainViewModelFactory(app.repository, app.settingsRepository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
        
        handleIntent(intent)
        
        setContent {
            val themeMode by viewModel.themeMode.collectAsState(initial = 0)
            
            val darkTheme = when (themeMode) {
                1 -> false
                2 -> true
                3 -> false // Android 17 Light Glass
                4 -> true  // Android 17 Dark Glass
                else -> isSystemInDarkTheme()
            }
            
            MyApplicationTheme(darkTheme = darkTheme) {
                BudgetApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.getBooleanExtra("open_add_transaction", false)) {
            val amount = intent.getDoubleExtra("prepopulate_amount", 0.0)
            val accountId = intent.getIntExtra("prepopulate_account_id", -1).takeIf { it != -1 }
            val categoryId = intent.getIntExtra("prepopulate_category_id", -1).takeIf { it != -1 }
            val typeStr = intent.getStringExtra("prepopulate_type") ?: "EXPENSE"
            val type = try { TransactionType.valueOf(typeStr) } catch (e: Exception) { TransactionType.EXPENSE }
            val note = intent.getStringExtra("prepopulate_note") ?: ""

            viewModel.setDraftTransaction(
                DraftTransaction(
                    amount = amount,
                    accountId = accountId,
                    categoryId = categoryId,
                    type = type,
                    note = note
                )
            )
        }
    }
}
