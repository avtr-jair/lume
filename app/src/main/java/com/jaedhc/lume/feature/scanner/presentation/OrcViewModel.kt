// feature/scanner/presentation/OcrViewModel.kt
package com.jaedhc.lume.feature.scanner.presentation

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaedhc.lume.domain.parser.model.OcrInput
import com.jaedhc.lume.domain.parser.model.OcrResult
import com.jaedhc.lume.domain.parser.services.DefaultOcrEngine
import com.jaedhc.lume.domain.parser.usecase.CreateTxFromTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val engine: DefaultOcrEngine,                 // reconoce + parsea + clasifica
    private val createTxFromText: CreateTxFromTextUseCase // si quieres persistir
) : ViewModel() {

    data class Item(val uri: String, val loading: Boolean, val result: OcrResult?, val error: String?)
    data class UiState(val items: List<Item> = emptyList(), val saving: Boolean = false)

    var uiState = androidx.compose.runtime.mutableStateOf(UiState())
        private set

    fun analyzeUris(resolver: ContentResolver, uris: List<android.net.Uri>, bankIdHint: Long? = null) {
        uiState.value = UiState(items = uris.map { Item(uri = it.toString(), loading = true, result = null, error = null) })
        uris.forEachIndexed { index, uri ->
            viewModelScope.launch {
                runCatching {
                    val bytes = readBytes(resolver, uri)
                    engine.analyze(OcrInput.Bytes(bytes, "image/*"))
                }.onSuccess { result ->
                    mutate(index) { it.copy(loading = false, result = result, error = null) }
                }.onFailure { e ->
                    mutate(index) { it.copy(loading = false, error = e.message ?: "Error OCR") }
                }
            }
        }
    }

    /** Guarda la transacción del item `index` usando el use case (opcional). */
    fun save(index: Int, income: Boolean, bankIdHint: Long? = null, last4Hint: String? = null) {
        val item = uiState.value.items.getOrNull(index)?.result ?: return
        viewModelScope.launch {
            uiState.value = uiState.value.copy(saving = true)
            runCatching {
                // usamos texto OCR completo para el use case
                createTxFromText(
                    input = OcrInput.PlainText(item.text),
                    bankIdHint = bankIdHint,
                    last4Hint = last4Hint,
                    income = income
                )
            }.onSuccess {
                // podrías marcarlo como “guardado” en el estado si quieres
            }.also {
                uiState.value = uiState.value.copy(saving = false)
            }
        }
    }

    private fun mutate(index: Int, transform: (Item) -> Item) {
        val list = uiState.value.items.toMutableList()
        list[index] = transform(list[index])
        uiState.value = uiState.value.copy(items = list)
    }

    private suspend fun readBytes(resolver: ContentResolver, uri: android.net.Uri): ByteArray =
        withContext(Dispatchers.IO) {
            resolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
        }
}
