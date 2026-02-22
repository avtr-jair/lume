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
import com.example.lume.data.Category
import com.example.lume.data.OcrResult
import com.example.lume.data.TxFields
import com.example.lume.network.LumeClient
import com.example.lume.processing.OcrStructuralExtractor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class ShareReceiverUiState {
    object Idle : ShareReceiverUiState()
    object Loading : ShareReceiverUiState()
    data class Success(val result: OcrResult) : ShareReceiverUiState()
    data class Error(val message: String) : ShareReceiverUiState()
}

class ShareReceiverViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ShareReceiverUiState>(ShareReceiverUiState.Idle)
    val uiState: StateFlow<ShareReceiverUiState> = _uiState.asStateFlow()

    fun processImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ShareReceiverUiState.Loading
            try {
                val resolver = getApplication<Application>().contentResolver
                // Run OCR and processing on IO thread
                val result = withContext(Dispatchers.IO) {
                    val text = runOcr(resolver, uri)
                    val extractor = OcrStructuralExtractor()
                    val structuralResult = extractor.extract(text)
                    
                    // User's categories (or generic list as requested)
                    val categories = listOf(
                        "Comida", "Transporte", "Entretenimiento", 
                        "Salud", "Finanzas", "Servicios", "Otros"
                    )
                    
                    val request = structuralResult.copy(categories = categories)

                    // Stage B: Send structural candidates + categories to backend
                    try {
                        Log.d("LumeNet", "Sending to Flask: $request")
                        val classifiedFields = LumeClient.apiService.classifyTransaction(request)
                        Log.d("LumeNet", "Received from Flask: $classifiedFields")
                        OcrResult(text = text, fields = classifiedFields, category = Category.OTROS)
                    } catch (e: Exception) {
                        Log.e("LumeNet", "Stage B classification failed", e)
                        // Fallback logic
                        val fallbackFields = TxFields(
                            amount = structuralResult.amount_candidates.maxOrNull(),
                            currency = if (text.contains("USD", ignoreCase = true)) "USD" else "MXN",
                            date = structuralResult.date_candidates.firstOrNull(),
                            merchant = structuralResult.text_lines.getOrNull(2) ?: "Unknown",
                            concept = structuralResult.text_lines.firstOrNull(),
                            category = "Otros",
                            type = "egreso" // Defaulting to egreso for safety
                        )
                        OcrResult(text = text, fields = fallbackFields, category = Category.OTROS)
                    }
                }
                _uiState.value = ShareReceiverUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = ShareReceiverUiState.Error("Failed to process image: ${e.message}")
            }
        }
    }

    private suspend fun runOcr(resolver: ContentResolver, uri: Uri): String {
        val image = InputImage.fromBitmap(loadBitmap(resolver, uri), 0)
        val recognizer = TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )
        val result = recognizer.process(image).await()
        Log.d("OCR", result.text)
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

    fun clearState() {
        _uiState.value = ShareReceiverUiState.Idle
    }
}
