package com.example.hustlefix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hustlefix.ui.viewmodels.PaymentMethod
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    methods: List<PaymentMethod>,
    onAddMethod: (String, String, String) -> Unit,
    onRemoveMethod: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Payment Methods", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Method", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("Your saved methods", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            if (methods.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No payment methods saved", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(methods) { method ->
                        PaymentMethodCard(
                            type = method.type, 
                            number = method.number, 
                            expiry = method.expiry,
                            onDelete = { onRemoveMethod(method.id) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("HustleFix Escrow", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Payments are held securely until the job is completed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPaymentDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, num, exp ->
                onAddMethod(type, num, exp)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PaymentMethodCard(type: String, number: String, expiry: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(number, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
                Text(expiry, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddPaymentDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var cardNum by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Visa") }
    var submitAttempted by remember { mutableStateOf(false) }
    val cardInvalid = cardNum.length !in 12..16 || !cardNum.all(Char::isDigit)
    val expiryParts = expiry.split("/")
    val expiryMonth = expiryParts.firstOrNull()?.toIntOrNull()
    val expiryYear = expiryParts.getOrNull(1)?.toIntOrNull()
    val now = Calendar.getInstance()
    val currentYear = now.get(Calendar.YEAR) % 100
    val currentMonth = now.get(Calendar.MONTH) + 1
    val expiryInvalid = !Regex("(0[1-9]|1[0-2])/[0-9]{2}").matches(expiry) ||
            expiryMonth == null || expiryYear == null ||
            expiryYear < currentYear || (expiryYear == currentYear && expiryMonth < currentMonth)
    val typeInvalid = type.isBlank()
    val formValid = !cardInvalid && !expiryInvalid && !typeInvalid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Payment Method", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = cardNum,
                    onValueChange = { if (it.length <= 16) cardNum = it },
                    label = { Text("Card Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    isError = (cardNum.isNotEmpty() && cardInvalid) || (submitAttempted && cardInvalid),
                    supportingText = {
                        if ((cardNum.isNotEmpty() && cardInvalid) || (submitAttempted && cardInvalid)) {
                            Text(if (cardNum.isEmpty()) "Card number is required" else "Enter 12–16 digits", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { if (it.length <= 5) expiry = it },
                        label = { Text("MM/YY") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        isError = (expiry.isNotEmpty() && expiryInvalid) || (submitAttempted && expiryInvalid),
                        supportingText = {
                            if ((expiry.isNotEmpty() && expiryInvalid) || (submitAttempted && expiryInvalid)) {
                                Text(if (expiry.isEmpty()) "Expiry is required" else "Use MM/YY", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text("Card Type") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        isError = submitAttempted && typeInvalid
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                submitAttempted = true
                if (formValid) onConfirm(type, cardNum, expiry)
            }) {
                Text("ADD CARD")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
