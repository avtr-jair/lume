package com.example.lume.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lume.data.db.AccountEntity
import com.example.lume.data.db.LumeDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CreateAccountUiState(
    val bankName: String = "",
    val last4: String = "",
    val accountTypeId: String = "DEBIT",
    val currency: String = "MXN",
    val initialBalance: String = "",
    val description: String = "",
    val selectedColor: String = "#FFB800",
    
    // TDC Specific
    val creditLimit: String = "",
    val closingDay: String = "",
    val dueDay: String = "",
    
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class CreateAccountViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumeDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()

    private val _uiState = MutableStateFlow(CreateAccountUiState())
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    fun setAccountType(typeId: String) {
        _uiState.update { it.copy(accountTypeId = typeId) }
    }

    fun onBankNameChange(name: String) {
        _uiState.update { it.copy(bankName = name) }
    }

    fun onLast4Change(last4: String) {
        if (last4.length <= 4 && last4.all { it.isDigit() }) {
            _uiState.update { it.copy(last4 = last4) }
        }
    }

    fun onBalanceChange(balance: String) {
        if (balance.isEmpty() || balance.toDoubleOrNull() != null || balance == "-") {
            _uiState.update { it.copy(initialBalance = balance) }
        }
    }

    fun onCreditLimitChange(limit: String) {
        if (limit.isEmpty() || limit.toDoubleOrNull() != null) {
            _uiState.update { it.copy(creditLimit = limit) }
        }
    }

    fun onClosingDayChange(day: String) {
        if (day.isEmpty() || (day.toIntOrNull() != null && day.toInt() in 1..31)) {
            _uiState.update { it.copy(closingDay = day) }
        }
    }

    fun onDueDayChange(day: String) {
        if (day.isEmpty() || (day.toIntOrNull() != null && day.toInt() in 1..31)) {
            _uiState.update { it.copy(dueDay = day) }
        }
    }

    fun onDescriptionChange(desc: String) {
        _uiState.update { it.copy(description = desc) }
    }

    fun onColorSelect(color: String) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun saveAccount() {
        val state = _uiState.value
        if (state.bankName.isBlank() || state.last4.length < 4) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val newAccount = AccountEntity(
                id = UUID.randomUUID().toString(),
                bankName = state.bankName,
                last4 = state.last4,
                label = state.description.takeIf { it.isNotBlank() },
                accountTypeId = state.accountTypeId,
                balance = state.initialBalance.toDoubleOrNull() ?: 0.0,
                color = state.selectedColor,
                creditLimit = state.creditLimit.toDoubleOrNull(),
                closingDay = state.closingDay.toIntOrNull(),
                dueDay = state.dueDay.toIntOrNull()
            )

            transactionDao.insertAccount(newAccount)
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}
