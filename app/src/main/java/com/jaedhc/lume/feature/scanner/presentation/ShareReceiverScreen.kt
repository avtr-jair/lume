package com.jaedhc.lume.feature.scanner.presentation
// ShareReceiverScreen.kt
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jaedhc.lume.domain.parser.model.OcrResult

@OptIn(ExperimentalMaterial3Api::class)
// ShareReceiverScreen.kt
@Composable
fun ShareReceiverScreen(
    uris: List<Uri>,
    onDone: () -> Unit,
    viewModel: OcrViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState

    LaunchedEffect(uris) {
        if (uris.isNotEmpty()) {
            viewModel.analyzeUris(context.contentResolver, uris)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compartido a mi app") },
                actions = { TextButton(onClick = onDone) { Text("Cerrar") } }
            )
        }
    ) { inner ->
        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner).padding(24.dp)) {
                Text("Sin imágenes")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(inner),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.items.size) { idx ->
                    val item = state.items[idx]
                    SharedCardItem(
                        uri = Uri.parse(item.uri),
                        loading = item.loading,
                        result = item.result,
                        onSave = { income -> viewModel.save(idx, income) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SharedCardItem(
    uri: Uri,
    loading: Boolean,
    result: OcrResult?,
    onSave: (income: Boolean) -> Unit
) {
    Card {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp))
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Leyendo… (OCR on-device)")
            } else if (result == null) {
                Text("No se pudo leer esta imagen")
            } else {
                Text("Texto OCR:", fontWeight = FontWeight.SemiBold)
                Text(result.text.take(500).ifEmpty { "(vacío)" })
                Divider()
                Text("Campos:", fontWeight = FontWeight.SemiBold)
                Text("Monto: ${result.fields.amount ?: "?"} ${result.fields.currency ?: ""}")
                Text("Fecha: ${result.fields.dateIso ?: "?"}")
                Text("Comercio/Beneficiario: ${result.fields.merchant ?: "?"}")
                Text("Concepto: ${result.fields.concept ?: "?"}")
                Text("Categoría: ${result.categoryId ?: "(sin clasificar)"}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onSave(false) }) { Text("Guardar como egreso") }
                    OutlinedButton(onClick = { onSave(true) }) { Text("Guardar como ingreso") }
                }
            }
        }
    }
}

