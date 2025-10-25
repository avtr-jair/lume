package com.jaedhc.lume.view.receiver
// ShareReceiverScreen.kt
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
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
import coil.compose.AsyncImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareReceiverScreen(
    uris: List<Uri>,
    onDone: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compartido a mi app") },
                actions = {
                    TextButton(onClick = onDone) { Text("Cerrar") }
                }
            )
        }
    ) { inner ->
        if (uris.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner).padding(24.dp)) {
                Text("Sin imágenes")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(inner),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uris) { uri ->
                    SharedCard(uri = uri)
                }
            }
        }
    }
}

@Composable
private fun SharedCard(uri: Uri) {
    val context = LocalContext.current
    val resolver = remember(context) { context.contentResolver }
    val ocrState = remember { mutableStateOf<OcrResult?>(null) }
    val loading = remember { mutableStateOf(false) }

    // Dispara OCR una vez por URI (procesa en memoria, no se guarda imagen)
    LaunchedEffect(uri) {
        loading.value = true
        val text = runCatching { runOcr(resolver, uri) }.getOrElse { "" }
        val parsed = parseFields(text)
        val cat = classify(parsed, text)
        ocrState.value = OcrResult(text = text, fields = parsed, category = cat)
        loading.value = false
    }

    Card {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
            if (loading.value) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Leyendo… (OCR on-device)")
            } else {
                val res = ocrState.value
                if (res == null) {
                    Text("No se pudo leer esta imagen")
                } else {
                    Text("Texto OCR:", fontWeight = FontWeight.SemiBold)
                    Text(res.text.take(500).ifEmpty { "(vacío)" })
                    Divider()
                    Text("Campos:", fontWeight = FontWeight.SemiBold)
                    Text("Monto: ${res.fields.amount ?: "?"} ${res.fields.currency ?: ""}")
                    Text("Fecha: ${res.fields.dateIso ?: "?"}")
                    Text("Comercio/Beneficiario: ${res.fields.merchant ?: "?"}")
                    Text("Concepto: ${res.fields.concept ?: "?"}")
                    Text("Categoría: ${res.category}")
                }
            }
        }
    }
}

/* ---------- OCR ---------- */

suspend fun runOcr(resolver: ContentResolver, uri: Uri): String {
    val image = InputImage.fromBitmap(loadBitmap(resolver, uri), 0)
    val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    ) // Latin ya viene incluido en este artefacto
    val result = recognizer.process(image).await()
    return result.text
}

// Decodifica a Bitmap en memoria (sin guardar)
private fun loadBitmap(resolver: ContentResolver, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(resolver, uri)
    }
}

/* ---------- Parseo + Clasificación (ejemplo simple) ---------- */

data class TxFields(
    val amount: Double?, val currency: String?,
    val dateIso: String?, val merchant: String?, val concept: String?
)

data class OcrResult(val text: String, val fields: TxFields, val category: Category)

enum class Category { COMIDA, TRANSPORTE, ENTRETENIMIENTO, SALUD, FINANZAS, SERVICIOS, OTROS }

fun parseFields(textRaw: String): TxFields {
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

fun classify(fields: TxFields, fullText: String): Category {
    val haystack = buildString {
        appendLine(fullText.lowercase())
        fields.merchant?.let { appendLine(it.lowercase()) }
        fields.concept?.let { appendLine(it.lowercase()) }
    }
    val hit = keywordMap.entries.firstOrNull { (_, keys) -> keys.any { haystack.contains(it) } }
    return hit?.key ?: Category.OTROS
}
