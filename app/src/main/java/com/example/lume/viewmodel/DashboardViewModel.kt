package com.example.lume.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lume.data.db.CategoryEntity
import com.example.lume.data.db.LumeDatabase
import com.example.lume.data.db.TransactionEntity
import com.example.lume.data.db.TransactionWithCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CategorySpend(
    val category: CategoryEntity,
    val amount: Double
)

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val categoriesBreakdown: List<CategorySpend> = emptyList(),
    val isSensitiveDataVisible: Boolean = true,
    val isLoading: Boolean = true
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumeDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            transactionDao.getAllTransactionsWithCategory().collect { transactionsWithCat ->
                // Move expensive processing to background thread
                val newState = withContext(Dispatchers.Default) {
                    val income = transactionsWithCat.filter { it.transaction.type == "ingreso" }.sumOf { it.transaction.amount }
                    val expenses = transactionsWithCat.filter { it.transaction.type == "egreso" }.sumOf { it.transaction.amount }
                    
                    val breakdown = transactionsWithCat
                        .filter { it.transaction.type == "egreso" && it.category != null }
                        .groupBy { it.category!! }
                        .map { (category, list) -> CategorySpend(category, list.sumOf { it.transaction.amount }) }
                        .sortedByDescending { it.amount }

                    _uiState.value.copy(
                        totalBalance = income - expenses,
                        totalIncome = income,
                        totalExpenses = expenses,
                        recentTransactions = transactionsWithCat.take(10),
                        categoriesBreakdown = breakdown,
                        isLoading = false
                    )
                }
                _uiState.value = newState
            }
        }
    }

    fun toggleSensitiveDataVisibility() {
        _uiState.update { it.copy(isSensitiveDataVisible = !it.isSensitiveDataVisible) }
    }
}
