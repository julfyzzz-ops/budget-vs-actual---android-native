package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.repository.BudgetRepository
import com.example.data.settings.SettingsRepository

class BudgetApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: BudgetRepository
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "budget_db")
            .fallbackToDestructiveMigration()
            .build()
        repository = BudgetRepository(database.budgetDao())
        settingsRepository = SettingsRepository(this)
    }
}
