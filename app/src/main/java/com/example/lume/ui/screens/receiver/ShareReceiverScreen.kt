package com.example.lume.ui.screens.receiver

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lume.data.Category
import com.example.lume.data.OcrResult
import com.example.lume.ui.components.ActiveGold
import com.example.lume.ui.components.BackgroundDark
import com.example.lume.ui.theme.SurfaceDark
import com.example.lume.ui.theme.TextGray
import com.example.lume.viewmodel.ShareReceiverUiState
import com.example.lume.viewmodel.ShareReceiverViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ShareReceiverScreen(
    uris: List<Uri>,
    onDone: () -> Unit,
    viewModel: ShareReceiverViewModel = viewModel()
) {
    // Top Bar is part of the custom design now, or we can use Scaffold topBar.
    // The design shows "Revisar Transacción" and a close button.
    
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            ReviewTopBar(onClose = onDone)
        }
        // BottomBar hoisted in AppNavigation, but we might want to hide it here if it covers the "Confirmar" button?
        // For now we assume it's visible.
    ) { innerPadding ->
        if (uris.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No images shared", color = Color.White)
            }
        } else {
            // If multiple URIs, we show them in a list. 
            // The design looks like a full screen view per item.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 100.dp) // Space for floating button or bottom bar
            ) {
                items(uris) { uri ->
                    TransactionReviewCard(uri = uri, viewModel = viewModel)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ReviewTopBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 32.dp, 16.dp, 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
        Text(
            text = "Revisar Transacción",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = {}) {
            Icon(Icons.Outlined.HelpOutline, contentDescription = "Help", tint = TextGray)
        }
    }
}

@Composable
fun TransactionReviewCard(
    uri: Uri,
    viewModel: ShareReceiverViewModel
) {
    // Logic to process image
    var localState by remember { mutableStateOf<ShareReceiverUiState>(ShareReceiverUiState.Idle) }
    
    LaunchedEffect(uri) {
        viewModel.processImage(uri)
    }

    val uiState by viewModel.uiState.collectAsState()
    
    // In a real multi-item list, we'd filter or verify the result matches this URI. 
    // For now taking the latest state as per previous agreement.
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        when (uiState) {
            is ShareReceiverUiState.Loading -> {
                CircularProgressIndicator(color = ActiveGold)
                Text("Analizando recibo...", color = TextGray)
            }
            is ShareReceiverUiState.Error -> {
                Text("Error al analizar", color = Color.Red)
            }
            is ShareReceiverUiState.Success -> {
                val result = (uiState as ShareReceiverUiState.Success).result
                TransactionForm(result)
            }
            else -> {}
        }
    }
}

@Composable
fun TransactionForm(result: OcrResult) {
    val amount = result.fields.amount ?: 0.0
    val currency = result.fields.currency ?: "MXN"
    // Format amount
    val formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(amount)

    // OCR Detected Badge
    Box(
        modifier = Modifier
            .background(Color(0xFF2A2A1E), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ActiveGold, modifier = Modifier.size(12.dp))
            Text("OCR DETECTADO", color = ActiveGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }

    // Amount
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formattedAmount,
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Extraído de tu captura de pantalla",
            color = Color(0xFF53535E),
            fontSize = 14.sp
        )
    }

    // Insight Chip (Placeholder)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E242E), RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF608BC1))
            Text("3 Meses sin intereses detectados", color = Color(0xFF608BC1), fontWeight = FontWeight.Medium)
        }
    }

    // Type Switch (Gasto / Ingreso)
    TransactionTypeSwitch()

    // Details Section
    Text(
        text = "DETALLES DE TRANSACCIÓN",
        color = TextGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
    ) {
        DetailRow(
            icon = Icons.Outlined.AccountBalanceWallet,
            label = "CUENTA",
            value = "BBVA Débito •••• 1234",
            showDivider = true
        )
        DetailRow(
            icon = Icons.Outlined.ShoppingBag,
            label = "CATEGORÍA",
            value = result.category.name.capitalize(), // Using OCR category
            showDivider = true
        )
        DetailRow(
            icon = Icons.Outlined.CalendarMonth,
            label = "FECHA",
            value = result.fields.dateIso ?: "Hoy, ${java.time.LocalDate.now()}",
            showDivider = false
        )
    }

    // Subscription Toggle
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2C2C35), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFA78BFA))
            }
            Column {
                Text("Suscripción", color = Color.White, fontWeight = FontWeight.Medium)
                Text("Repetir cada mes", color = TextGray, fontSize = 12.sp)
            }
        }
        Switch(
            checked = false,
            onCheckedChange = {},
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = ActiveGold,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = BackgroundDark
            )
        )
    }

    // Note Input
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Notes, contentDescription = null, tint = TextGray)
            Text("Agregar una nota sobre esta transacción...", color = TextGray)
        }
    }

    // Confirm Button
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(containerColor = ActiveGold),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Confirmar y Guardar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
    
    Spacer(Modifier.height(16.dp))
    Text("LUME AI • INTELIGENCIA FINANCIERA", color = Color(0xFF333333), fontSize = 10.sp, letterSpacing = 1.sp)
}

@Composable
fun TransactionTypeSwitch() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        // Gasto (Selected)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Transparent, RoundedCornerShape(10.dp)), // Deselected style
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFFEF4444))
                Text("Gasto", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
            }
        }
        
        // Ingreso (Selected - Mocking the layout logic)
        // Ideally state controls which one is highlighted
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(ActiveGold, RoundedCornerShape(10.dp)), // Selected style
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.Black)
                Text("Ingreso", color = Color.Black, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2C2C35), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TextGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(value, color = Color.White, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextGray)
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp), // Indented divider
                thickness = 1.dp,
                color = Color(0xFF2C2C35)
            )
        }
    }
}

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
