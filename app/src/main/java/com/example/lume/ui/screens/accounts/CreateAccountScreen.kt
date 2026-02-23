package com.example.lume.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.lume.ui.components.ActiveGold
import com.example.lume.ui.components.BackgroundDark
import com.example.lume.ui.theme.SurfaceDark
import com.example.lume.ui.theme.TextGray
import com.example.lume.viewmodel.CreateAccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    navController: NavHostController,
    typeId: String,
    viewModel: CreateAccountViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(typeId) {
        viewModel.setAccountType(typeId)
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.navigate(com.example.lume.ui.navigation.Screen.ManageAccounts.route) {
                popUpTo(com.example.lume.ui.navigation.Screen.ManageAccounts.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Column(modifier = Modifier.background(BackgroundDark)) {
                TopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("STEP 2 OF 2", color = ActiveGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Nueva Cuenta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        // Spacer to balance the back button and keep title centered
                        Spacer(modifier = Modifier.size(48.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark),
                    windowInsets = WindowInsets(0)
                )
                // Custom Progress Bar (Full Width)
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(ActiveGold))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            AccountPreviewCard(
                bank = uiState.bankName,
                last4 = uiState.last4,
                balance = uiState.initialBalance,
                color = uiState.selectedColor,
                typeId = uiState.accountTypeId,
                creditLimit = uiState.creditLimit
            )

            // Form Fields
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormField(
                    label = if (uiState.accountTypeId == "CREDIT") "NOMBRE DE LA TARJETA" else "NOMBRE DEL BANCO",
                    value = uiState.bankName,
                    onValueChange = { viewModel.onBankNameChange(it) },
                    placeholder = if (uiState.accountTypeId == "CREDIT") "Ej. RappiCard, HSBC Viva..." else "Ej. BBVA, Santander, Nu...",
                    icon = if (uiState.accountTypeId == "CREDIT") Icons.Default.CreditCard else Icons.Default.AccountBalance
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FormField(
                        modifier = Modifier.weight(1f),
                        label = "ÚLTIMOS 4 DÍGITOS",
                        value = uiState.last4,
                        onValueChange = { viewModel.onLast4Change(it) },
                        placeholder = "0000",
                        icon = Icons.Default.Tag,
                        keyboardType = KeyboardType.Number
                    )
                    FormField(
                        modifier = Modifier.weight(1f),
                        label = "DIVISA",
                        value = uiState.currency,
                        onValueChange = { },
                        placeholder = "MXN",
                        icon = Icons.Default.Payments,
                        enabled = false
                    )
                }

                if (uiState.accountTypeId == "CREDIT") {
                    FormField(
                        label = "LÍMITE DE CRÉDITO",
                        value = uiState.creditLimit,
                        onValueChange = { viewModel.onCreditLimitChange(it) },
                        placeholder = "$ 0.00",
                        prefix = "$ ",
                        keyboardType = KeyboardType.Decimal
                    )
                }

                FormField(
                    label = if (uiState.accountTypeId == "CREDIT") "SALDO AL CORTE / DEUDA ACTUAL" else "BALANCE INICIAL",
                    value = uiState.initialBalance,
                    onValueChange = { viewModel.onBalanceChange(it) },
                    placeholder = "$ 0.00",
                    prefix = "$ ",
                    keyboardType = KeyboardType.Decimal
                )

                if (uiState.accountTypeId == "CREDIT") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FormField(
                            modifier = Modifier.weight(1f),
                            label = "DÍA DE CORTE",
                            value = uiState.closingDay,
                            onValueChange = { viewModel.onClosingDayChange(it) },
                            placeholder = "1-31",
                            icon = Icons.Default.Event,
                            keyboardType = KeyboardType.Number
                        )
                        FormField(
                            modifier = Modifier.weight(1f),
                            label = "DÍA DE PAGO",
                            value = uiState.dueDay,
                            onValueChange = { viewModel.onDueDayChange(it) },
                            placeholder = "1-31",
                            icon = Icons.Default.CalendarToday,
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                FormField(
                    label = "DESCRIPCIÓN (OPCIONAL)",
                    value = uiState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    placeholder = "Ej. Gastos personales, Nómina...",
                    icon = Icons.Default.Notes
                )
            }

            ColorSelector(uiState.selectedColor) { viewModel.onColorSelect(it) }

            SecurityNote()

            Button(
                onClick = { viewModel.saveAccount() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ActiveGold),
                enabled = !uiState.isSaving && uiState.bankName.isNotBlank() && uiState.last4.length == 4
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Guardar Cuenta", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AccountPreviewCard(
    bank: String, 
    last4: String, 
    balance: String, 
    color: String,
    typeId: String,
    creditLimit: String
) {
    val displayColor = Color(android.graphics.Color.parseColor(color))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LUME SMART MANAGER", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (bank.isBlank()) (if (typeId == "CREDIT") "Nombre de Tarjeta" else "Nombre del Banco") else bank,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "••••  ••••  ••••  ${if (last4.isBlank()) "0000" else last4}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    letterSpacing = 2.sp
                )
            }
            
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(if (typeId == "CREDIT") "DEUDA ACTUAL" else "BALANCE INICIAL", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    "$${if (balance.isBlank()) "0.00" else balance}",
                    color = displayColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    if (typeId == "CREDIT") Icons.Default.CreditCard else Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = displayColor,
                    modifier = Modifier.size(32.dp)
                )
                if (typeId == "CREDIT" && creditLimit.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Límite: $$creditLimit", color = TextGray, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextGray.copy(alpha = 0.5f)) },
            leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = ActiveGold) } },
            prefix = prefix?.let { { Text(it, color = ActiveGold) } },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                disabledContainerColor = SurfaceDark,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = ActiveGold,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            enabled = enabled,
            singleLine = true
        )
    }
}

@Composable
fun ColorSelector(selectedColor: String, onSelect: (String) -> Unit) {
    val colors = listOf("#FFB800", "#2ECC71", "#3498DB", "#E74C3C", "#9B59B6", "#E91E63", "#F39C12", "#1abc9c")
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("COLOR DE CUENTA", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEach { colorStr ->
                val color = Color(android.graphics.Color.parseColor(colorStr))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedColor == colorStr) 2.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clickable { onSelect(colorStr) }
                )
            }
        }
    }
}

@Composable
fun SecurityNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.Shield, contentDescription = null, tint = ActiveGold, modifier = Modifier.size(24.dp))
        Text(
            "Sus datos bancarios están cifrados y se almacenan únicamente en su dispositivo. Lume nunca comparte su información financiera.",
            color = TextGray,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}
