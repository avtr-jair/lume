package com.example.lume.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.lume.data.db.CategoryEntity
import com.example.lume.data.db.TransactionEntity
import com.example.lume.data.db.TransactionWithCategory
import com.example.lume.ui.components.ActiveGold
import com.example.lume.ui.components.BackgroundDark
import com.example.lume.ui.theme.SurfaceDark
import com.example.lume.ui.theme.TextGray
import com.example.lume.viewmodel.CategorySpend
import com.example.lume.viewmodel.DashboardUiState
import com.example.lume.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = 32.dp, bottom = 120.dp) // Added more space for FAB/Bar
    ) {
        item { DashboardHeader() }
        item {
            TotalBalanceSection(
                balance = uiState.totalBalance,
                isVisible = uiState.isSensitiveDataVisible,
                onToggleVisibility = { viewModel.toggleSensitiveDataVisibility() }
            )
        }
        item {
            IncomeExpenseSummary(
                income = uiState.totalIncome,
                expenses = uiState.totalExpenses,
                isVisible = uiState.isSensitiveDataVisible
            )
        }
        item {
            CategorySpendingSection(
                breakdown = uiState.categoriesBreakdown,
                totalExpenses = uiState.totalExpenses,
                isVisible = uiState.isSensitiveDataVisible
            )
        }
        items(
            items = uiState.recentTransactions,
            key = { it.transaction.id } // STABLE KEY for performance
        ) { txWithCat ->
            TransactionItem(txWithCat, isVisible = uiState.isSensitiveDataVisible)
        }
        
        item { Spacer(modifier = Modifier.height(140.dp)) } // More robust than contentPadding
    }
}

@Composable
fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }
            Column {
                Text("BUENOS DÍAS,", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Usuario", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        IconButton(
            onClick = { },
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceDark)
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Color.White)
        }
    }
}

@Composable
fun TotalBalanceSection(
    balance: Double,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("SALDO TOTAL", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Toggle Visibility",
                tint = TextGray,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onToggleVisibility() }
            )
        }
        Text(
            text = if (isVisible) formatCurrency(balance) else "***",
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .background(Color(0xFF1E241E), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ActiveGold, modifier = Modifier.size(12.dp))
                Text("+2.4% ESTE MES", color = ActiveGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun IncomeExpenseSummary(income: Double, expenses: Double, isVisible: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryCard(
            label = "INGRESOS",
            amount = income,
            icon = Icons.Default.ArrowDownward, 
            iconColor = Color(0xFF2D9F24),
            modifier = Modifier.weight(1f),
            isVisible = isVisible
        )
        SummaryCard(
            label = "EGRESOS",
            amount = expenses,
            icon = Icons.Default.ArrowUpward,
            iconColor = Color(0xFFEF4444),
            modifier = Modifier.weight(1f),
            isVisible = isVisible
        )
    }
}

@Composable
fun SummaryCard(
    label: String,
    amount: Double,
    icon: ImageVector,
    iconColor: Color,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
            }
            Text(label, color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = if (isVisible) formatCurrency(amount) else "***",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CategorySpendingSection(breakdown: List<CategorySpend>, totalExpenses: Double, isVisible: Boolean) {
    val currentMonth = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()).uppercase() }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(24.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Gastos por Categoría", color = Color.White, fontWeight = FontWeight.Bold)
            Text(currentMonth, color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            DonutChart(breakdown, totalExpenses)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isVisible) formatCurrency(totalExpenses) else "***",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("TOTAL GASTADO", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Legend
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val chunked = remember(breakdown) { breakdown.chunked(2) }
            chunked.forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { spend ->
                        val percentage = remember(spend.amount, totalExpenses) { 
                            if (totalExpenses > 0) (spend.amount / totalExpenses * 100).toInt() else 0 
                        }
                        val categoryColor = remember(spend.category.color) { 
                            spend.category.color?.let { try { Color(android.graphics.Color.parseColor(it)) } catch(e: Exception) { ActiveGold } } ?: ActiveGold
                        }
                        CategoryLegendItem(
                            label = spend.category.name,
                            percentage = percentage,
                            color = categoryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(breakdown: List<CategorySpend>, total: Double) {
    Canvas(modifier = Modifier.size(200.dp)) {
        var startAngle = -90f
        if (total == 0.0) {
            drawArc(
                color = Color.DarkGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
            )
        } else {
            breakdown.forEach { spend ->
                val sweepAngle = (spend.amount / total * 360f).toFloat()
                val categoryColor = spend.category.color?.let { try { Color(android.graphics.Color.parseColor(it)) } catch(e: Exception) { ActiveGold } } ?: ActiveGold
                drawArc(
                    color = categoryColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun CategoryLegendItem(label: String, percentage: Int, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text("${label.capitalize()} ($percentage%)", color = TextGray, fontSize = 12.sp)
    }
}

@Composable
fun RecentTransactionsHeader() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Transacciones recientes", color = Color.White, fontWeight = FontWeight.Bold)
        Text("VER TODO", color = ActiveGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TransactionItem(txWithCat: TransactionWithCategory, isVisible: Boolean) {
    val transaction = txWithCat.transaction
    val category = txWithCat.category
    
    val categoryColor = remember(category?.color) { 
        category?.color?.let { try { Color(android.graphics.Color.parseColor(it)) } catch(e: Exception) { TextGray } } ?: TextGray 
    }
    
    val categoryIcon = remember(category?.icon) { 
        getCategoryIcon(category?.icon ?: "Category") 
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(categoryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                categoryIcon,
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.merchant ?: transaction.concept ?: "Transacción",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(transaction.dateIso, color = TextGray, fontSize = 12.sp)
        }
        Text(
            text = (if (transaction.type == "egreso") "-" else "+") + (if (isVisible) formatCurrency(transaction.amount) else "***"),
            color = if (transaction.type == "egreso") Color.White else Color(0xFF2D9F24),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}

private fun getCategoryIcon(iconName: String): ImageVector {
    val normalizedName = iconName.substringAfterLast('.')
    return when (normalizedName) {
        "Restaurant" -> Icons.Default.Restaurant
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "ConfirmationNumber" -> Icons.Default.ConfirmationNumber
        "MedicalServices" -> Icons.Default.MedicalServices
        "Payments" -> Icons.Default.Payments
        "Lightbulb" -> Icons.Default.Lightbulb
        "Receipt" -> Icons.Default.Receipt
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "Home" -> Icons.Default.Home
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "Add" -> Icons.Default.Add
        else -> Icons.Default.Category
    }
}

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
