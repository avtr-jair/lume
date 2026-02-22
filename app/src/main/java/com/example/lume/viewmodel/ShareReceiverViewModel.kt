package com.example.lume.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lume.data.OcrResult
import com.example.lume.data.TxFields
import com.example.lume.data.db.LumeDatabase
import com.example.lume.data.db.TransactionEntity
import com.example.lume.network.LumeClient
import com.example.lume.processing.OcrStructuralExtractor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ShareReceiverUiState {
    object Idle : ShareReceiverUiState()
    object Loading : ShareReceiverUiState()
    data class Success(val result: OcrResult) : ShareReceiverUiState()
    data class Error(val message: String) : ShareReceiverUiState()
}

class ShareReceiverViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ShareReceiverUiState>(ShareReceiverUiState.Idle)
    val uiState: StateFlow<ShareReceiverUiState> = _uiState.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess: SharedFlow<Unit> = _saveSuccess.asSharedFlow()

    private val db = LumeDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()

    val categories = transactionDao.getAllCategories()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    fun onCategorySelected(id: String) {
        _selectedCategoryId.value = id
    }

    fun processImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ShareReceiverUiState.Loading
            try {
                val resolver = getApplication<Application>().contentResolver
                val result = withContext(Dispatchers.IO) {
                    val text = runOcr(resolver, uri)
                    val extractor = OcrStructuralExtractor()
                    val structuralResult = extractor.extract(text)
                    
                    val dbCategories = transactionDao.getCategoriesSnapshot()
                    val categoryNames = dbCategories.map { it.name }
                    
                    val request = structuralResult.copy(categories = categoryNames)

                    val classifiedFields = try {
                        LumeClient.apiService.classifyTransaction(request)
                    } catch (e: Exception) {
                        Log.e("LumeNet", "Stage B classification failed", e)
                        TxFields(
                            amount = structuralResult.amount_candidates.maxOrNull(),
                            currency = if (text.contains("USD", ignoreCase = true)) "USD" else "MXN",
                            date = structuralResult.date_candidates.firstOrNull(),
                            merchant = structuralResult.text_lines.getOrNull(2) ?: "Unknown",
                            concept = structuralResult.text_lines.firstOrNull(),
                            category = "Otros",
                            type = "egreso"
                        )
                    }

                    // Map backend category name to DB entity
                    val matchedCategory = dbCategories.find { 
                        it.name.equals(classifiedFields.category, ignoreCase = true) 
                    } ?: dbCategories.find { it.id == "otros" } ?: dbCategories.firstOrNull()

                    OcrResult(
                        text = text, 
                        fields = classifiedFields, 
                        selectedCategoryId = matchedCategory?.id ?: "otros",
                        suggestedCategory = matchedCategory
                    )
                }
                
                _selectedCategoryId.value = result.selectedCategoryId
                _uiState.value = ShareReceiverUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = ShareReceiverUiState.Error("Failed to process image: ${e.message}")
            }
        }
    }

    private suspend fun runOcr(resolver: ContentResolver, uri: Uri): String {
        val image = InputImage.fromBitmap(loadBitmap(resolver, uri), 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = recognizer.process(image).await()
        return result.text
    }

    private fun loadBitmap(resolver: ContentResolver, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(resolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(resolver, uri)
        }
    }

    fun saveTransaction(
        result: OcrResult,
        type: String,
        isSubscription: Boolean,
        note: String?
    ) {
        viewModelScope.launch {
            try {
                val transaction = TransactionEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    amount = result.fields.amount ?: 0.0,
                    currency = result.fields.currency ?: "MXN",
                    dateIso = result.fields.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    merchant = result.fields.merchant,
                    concept = result.fields.concept,
                    categoryId = _selectedCategoryId.value ?: "otros",
                    accountId = null,
                    type = type,
                    isSubscription = isSubscription,
                    note = note
                )
                withContext(Dispatchers.IO) {
                    transactionDao.insertTransaction(transaction)
                }
                _saveSuccess.emit(Unit)
            } catch (e: Exception) {
                Log.e("LumeDB", "Failed to save transaction", e)
            }
        }
    }

    fun clearState() {
        _uiState.value = ShareReceiverUiState.Idle
    }
}
