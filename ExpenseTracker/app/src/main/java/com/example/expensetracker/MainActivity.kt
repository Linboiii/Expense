package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ExpenseScreen()
            }
        }
    }
}

@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editItem by remember { mutableStateOf<Expense?>(null) }

    ExpenseContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onAmountChange = viewModel::onAmountChange,
        onSubmit = viewModel::onSubmit,
        onDelete = viewModel::deleteExpense,
        onEdit = { editItem = it }
    )

    if (editItem != null) {
        EditDialog(
            expense = editItem!!,
            onDismiss = { editItem = null },
            onSave = { id, amount ->
                viewModel.updateAmount(id, amount)
                editItem = null
            }
        )
    }
}

@Composable
fun ExpenseContent(
    uiState: ExpenseUiState,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDelete: (id: String) -> Unit,
    onEdit: (Expense) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF7F7FD)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    "Expenses",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Total Expense : ${formatRp(uiState.total)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { onNameChange(it) },
                    label = { Text("Expense Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = { onAmountChange(it) },
                        label = { Text("Amount of Expense") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.amountError,
                        supportingText = {
                            if (uiState.amountError) Text("Amount must be greater than 0")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(12.dp))

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEBEBF5),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onSubmit() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.list, key = { it.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE5E7FA),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Name : ${item.name}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text(formatRp(item.amount), fontSize = 13.sp, color = Color.Gray)
                                }
                                Row {
                                    IconButton(onClick = { onEdit(item) }, modifier = Modifier.size(32.dp)) {
                                        Text("✏️", fontSize = 14.sp)
                                    }
                                    IconButton(onClick = { onDelete(item.id) }, modifier = Modifier.size(32.dp)) {
                                        Text("🗑️", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var txt by remember { mutableStateOf(expense.amount.toString()) }
    var err by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Amount") },
        text = {
            Column {
                Text(expense.name, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = txt,
                    onValueChange = { txt = it; err = false },
                    label = { Text("New Amount") },
                    isError = err,
                    supportingText = { if (err) Text("Amount must be greater than 0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val num = txt.toLongOrNull() ?: 0L
                if (num <= 0L) {
                    err = true
                } else {
                    onSave(expense.id, num)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun formatRp(amount: Long): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    fmt.maximumFractionDigits = 0
    return fmt.format(amount).replace("Rp", "Rp ").replace(",00", "")
}