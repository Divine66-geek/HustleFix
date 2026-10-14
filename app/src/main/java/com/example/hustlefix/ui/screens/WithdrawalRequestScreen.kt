package com.example.hustlefix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalRequestScreen(
    availableBalance: String,
    isLoading: Boolean,
    onWithdrawClick: (Double, String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accountHolder by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var branchCode by remember { mutableStateOf("") }
    var submitAttempted by remember { mutableStateOf(false) }
    val amountValue = amount.toDoubleOrNull()
    val amountInvalid = amountValue == null || !amountValue.isFinite() || amountValue <= 0.0
    val bankInvalid = bankName.isBlank()
    val holderInvalid = accountHolder.isBlank()
    val accountInvalid = accountNumber.isBlank() || !accountNumber.all(Char::isDigit)
    val branchInvalid = branchCode.isBlank() || !branchCode.all(Char::isDigit)
    val formValid = !amountInvalid && !bankInvalid && !holderInvalid && !accountInvalid && !branchInvalid
    
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Withdraw Funds", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Available Balance", style = MaterialTheme.typography.labelLarge)
                    Text(availableBalance, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Withdrawal Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount to Withdraw (R)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                prefix = { Text("R ") },
                isError = (amount.isNotEmpty() && amountInvalid) || (submitAttempted && amountInvalid),
                supportingText = {
                    if ((amount.isNotEmpty() && amountInvalid) || (submitAttempted && amountInvalid)) {
                        Text("Enter an amount greater than zero", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = submitAttempted && bankInvalid
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = accountHolder,
                onValueChange = { accountHolder = it },
                label = { Text("Account Holder Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = submitAttempted && holderInvalid
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = { Text("Account Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                isError = (accountNumber.isNotEmpty() && accountInvalid) || (submitAttempted && accountInvalid),
                supportingText = {
                    if ((accountNumber.isNotEmpty() && accountInvalid) || (submitAttempted && accountInvalid)) {
                        Text(if (accountNumber.isBlank()) "Account number is required" else "Enter digits only", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = branchCode,
                onValueChange = { branchCode = it },
                label = { Text("Branch Code") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                isError = (branchCode.isNotEmpty() && branchInvalid) || (submitAttempted && branchInvalid),
                supportingText = {
                    if ((branchCode.isNotEmpty() && branchInvalid) || (submitAttempted && branchInvalid)) {
                        Text(if (branchCode.isBlank()) "Branch code is required" else "Enter digits only", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    submitAttempted = true
                    if (formValid) onWithdrawClick(amountValue!!, bankName, accountHolder, accountNumber, branchCode)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("REQUEST WITHDRAWAL", fontWeight = FontWeight.ExtraBold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Withdrawals are processed within 2-3 business days. A small platform fee may apply.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
