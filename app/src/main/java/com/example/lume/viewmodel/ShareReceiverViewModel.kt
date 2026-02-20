package com.example.lume.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lume.data.Category
import com.example.lume.data.OcrResult
import com.example.lume.data.TxFields
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
                    val parsed = parseFields(text)
                    val cat = classify(parsed, text)
                    OcrResult(text = text, fields = parsed, category = cat)
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

    private fun parseFields(textRaw: String): TxFields {
        val t = textRaw.replace(",", "").trim()

        val amount = Regex("""\$(\d+(?:\.\d{1,2})?)""")
            .findAll(t)
            .mapNotNull { it.groupValues.getOrNull(1)?.toDoubleOrNull() }
            .maxOrNull()

        val currency = when {
            Regex("""\bMXN|\$""", RegexOption.IGNORE_CASE).containsMatchIn(t) -> "MXN"
            Regex("""\bUSD\b""", RegexOption.IGNORE_CASE).containsMatchIn(t) -> "USD"
            else -> null
        }

        val dateRaw = Regex("""\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})\b""").find(t)?.value
            ?: Regex("""\b(\d{1,2}\s+(?:ene|feb|mar|abr|may|jun|jul|ago|sep|oct|nov|dic)\w*\s+\d{2,4})\b""",
                RegexOption.IGNORE_CASE).find(t)?.value
        val dateIso = dateRaw?.let { normalizeToIso(it) }

        val merchant = Regex("""(?i)(?:comercio|beneficiario|establecimiento|receptor|concepto)\s*:\s*(.+)""")
            .find(t)?.groupValues?.getOrNull(1)?.lineOrFirstWord()

        val concept = Regex("""(?i)concepto\s*:\s*(.+)""").find(t)?.groupValues?.getOrNull(1)?.lineOrFirstWord()
            ?: merchant

        return TxFields(amount, currency, dateIso, merchant, concept)
    }

    private fun String.lineOrFirstWord(): String = this.lines().firstOrNull()?.trim() ?: this.trim()

    private fun normalizeToIso(s: String): String? {
        val m1 = Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})""").matchEntire(s)
        if (m1 != null) {
            val d = m1.groupValues[1].padStart(2, '0')
            val mo = m1.groupValues[2].padStart(2, '0')
            val y = m1.groupValues[3].let { if (it.length==2) "20$it" else it }
            return "$y-$mo-$d"
        }
        return null
    }

    private val keywordMap = mapOf(
        Category.TRANSPORTE to listOf("uber","didi","cabify","metro","autobús","gasolina","pemex"),
        Category.ENTRETENIMIENTO to listOf("netflix","spotify","xbox","playstation","cine","cinépolis","cinepolis"),
        Category.COMIDA to listOf("ubereats","rappi","domino","kfc","mcdonald","restaurante","taquer"),
        Category.SALUD to listOf("farmacia","doctor","dent","laboratorio","hospital"),
        Category.FINANZAS to listOf("comisión","retiro","depósito","deposito","transferencia","spei","nómina","nomina"),
        Category.SERVICIOS to listOf("telmex","izzi","cfe","luz","agua","internet")
    )

    private fun classify(fields: TxFields, fullText: String): Category {
        val haystack = buildString {
            appendLine(fullText.lowercase())
            fields.merchant?.let { appendLine(it.lowercase()) }
            fields.concept?.let { appendLine(it.lowercase()) }
        }
        val hit = keywordMap.entries.firstOrNull { (_, keys) -> keys.any { haystack.contains(it) } }
        return hit?.key ?: Category.OTROS
    }

    fun clearState() {
        _uiState.value = ShareReceiverUiState.Idle
    }
}
