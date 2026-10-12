package com.example.hustlefix.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hustlefix.Transaction
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeStatementScreen(
    transactions: List<Transaction>,
    totalIncome: Double,
    totalFees: Double,
    netPay: Double,
    isLoading: Boolean,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Income Statement", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Summary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Earnings Summary", style = MaterialTheme.typography.labelMedium)
                        Text("R${String.format("%.2f", netPay)}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Gross Income", style = MaterialTheme.typography.labelSmall)
                                Text("R${String.format("%.2f", totalIncome)}", fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Platform Fees", style = MaterialTheme.typography.labelSmall)
                                Text("- R${String.format("%.2f", totalFees)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Download / Share PDF Report Button
                Button(
                    onClick = {
                        try {
                            val pdfFile = generateIncomeStatementPdf(context, netPay, totalIncome, totalFees, transactions)
                            if (pdfFile != null) {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                val chooser = Intent.createChooser(intent, "Download / Share PDF Report").apply {
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(chooser)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error generating PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DOWNLOAD / SHARE PDF REPORT", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Transaction Details", modifier = Modifier.padding(horizontal = 24.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                if (transactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No income recorded yet", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(transactions) { tx ->
                            IncomeItem(tx)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color.LightGray.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeItem(tx: Transaction) {
    val isPositive = tx.amount >= 0
    val amountColor = if (isPositive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    val sign = if (isPositive) "+ R" else "- R"
    val title = tx.type ?: tx.serviceTitle ?: "Transaction"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = amountColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isPositive) Icons.Default.TrendingUp else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(tx.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        
        Column(horizontalAlignment = Alignment.End) {
            val txAmount = tx.amount ?: 0.0
            val fee = if (txAmount > 0) txAmount * 0.10 else 0.0
            Text("$sign${String.format("%.2f", abs(txAmount))}", fontWeight = FontWeight.Black, color = amountColor)
            Text("Fee: R${String.format("%.2f", fee)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

fun generateIncomeStatementPdf(
    context: Context,
    netPay: Double,
    totalIncome: Double,
    totalFees: Double,
    transactions: List<Transaction>
): File? {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Paints
        val bgPaint = Paint().apply { color = android.graphics.Color.WHITE }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        val headerPaint = Paint().apply { color = android.graphics.Color.rgb(108, 99, 255) } // Primary brand color
        canvas.drawRect(0f, 0f, 595f, 110f, headerPaint)

        val titlePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint().apply {
            color = android.graphics.Color.argb(220, 255, 255, 255)
            textSize = 12f
        }
        val cardBgPaint = Paint().apply {
            color = android.graphics.Color.rgb(245, 247, 250)
            style = Paint.Style.FILL
        }
        val textPrimary = Paint().apply {
            color = android.graphics.Color.rgb(33, 33, 33)
            textSize = 14f
            isFakeBoldText = true
        }
        val textSecondary = Paint().apply {
            color = android.graphics.Color.rgb(117, 117, 117)
            textSize = 12f
        }
        val greenPaint = Paint().apply {
            color = android.graphics.Color.rgb(76, 175, 80)
            textSize = 14f
            isFakeBoldText = true
        }
        val redPaint = Paint().apply {
            color = android.graphics.Color.rgb(244, 67, 54)
            textSize = 14f
            isFakeBoldText = true
        }
        val linePaint = Paint().apply {
            color = android.graphics.Color.rgb(220, 224, 230)
            strokeWidth = 1f
        }

        // Header Content
        canvas.drawText("HUSTLEFIX", 40f, 50f, titlePaint)
        canvas.drawText("Official Income Statement & Earnings Report", 40f, 75f, subtitlePaint)
        canvas.drawText("Generated: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}", 40f, 95f, subtitlePaint)

        // Summary Card Box
        canvas.drawRoundRect(40f, 135f, 555f, 255f, 16f, 16f, cardBgPaint)
        
        canvas.drawText("EARNINGS SUMMARY (NET PAY)", 65f, 170f, textSecondary)
        
        val netPayPaint = Paint().apply {
            color = android.graphics.Color.rgb(108, 99, 255)
            textSize = 28f
            isFakeBoldText = true
        }
        canvas.drawText("R${String.format("%.2f", netPay)}", 65f, 210f, netPayPaint)

        canvas.drawLine(65f, 225f, 530f, 225f, linePaint)

        canvas.drawText("Gross Income: R${String.format("%.2f", totalIncome)}", 65f, 245f, textPrimary)
        canvas.drawText("Platform Fees (-10%): -R${String.format("%.2f", totalFees)}", 330f, 245f, redPaint)

        // Transaction Details Section Header
        canvas.drawText("TRANSACTION HISTORY", 40f, 300f, textPrimary)
        canvas.drawLine(40f, 310f, 555f, 310f, linePaint)

        // Table Header
        val tableHeaderPaint = Paint().apply {
            color = android.graphics.Color.rgb(100, 100, 100)
            textSize = 11f
            isFakeBoldText = true
        }
        canvas.drawText("DATE", 40f, 335f, tableHeaderPaint)
        canvas.drawText("DESCRIPTION / TYPE", 170f, 335f, tableHeaderPaint)
        canvas.drawText("AMOUNT", 470f, 335f, tableHeaderPaint)
        canvas.drawLine(40f, 345f, 555f, 345f, linePaint)

        // Transactions List
        var y = 370f
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        for (tx in transactions) {
            if (y > 780f) break
            val dateStr = dateFormat.format(Date(tx.timestamp))
            val title = tx.type ?: tx.serviceTitle ?: "Transaction"
            val amtStr = "${if (tx.amount >= 0) "+R" else "-R"}${String.format("%.2f", abs(tx.amount))}"
            val rowPaint = if (tx.amount >= 0) greenPaint else redPaint

            canvas.drawText(dateStr, 40f, y, textSecondary)
            canvas.drawText(title, 170f, y, textPrimary)
            canvas.drawText(amtStr, 470f, y, rowPaint)

            y += 28f
            canvas.drawLine(40f, y - 8f, 555f, y - 8f, linePaint)
        }

        // Footer
        val footerPaint = Paint().apply {
            color = android.graphics.Color.rgb(150, 150, 150)
            textSize = 10f
        }
        canvas.drawText("HustleFix Marketplace • Secure Escrow & Payout System", 40f, 810f, footerPaint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "HustleFix_Income_Statement.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
