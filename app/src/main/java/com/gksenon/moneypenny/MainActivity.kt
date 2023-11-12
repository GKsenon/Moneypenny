package com.gksenon.moneypenny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gksenon.moneypenny.ui.theme.MoneypennyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneypennyTheme {
                var showStartDialog by remember { mutableStateOf(false) }
                StartScreen(onStartButtonClicked = { showStartDialog = true })
                if (showStartDialog) {
                    StartDialog(
                        onConfirmButtonClicked = {
                            println(it)
                            showStartDialog = false
                        },
                        onDismissButtonClicked = { showStartDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
fun StartScreen(onStartButtonClicked: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        ElevatedButton(onClick = onStartButtonClicked) {
            Text(text = stringResource(id = R.string.start_game))
        }
    }
}

@Composable
fun StartDialog(
    onConfirmButtonClicked: (Int) -> Unit,
    onDismissButtonClicked: () -> Unit
) {
    var startingMoney by remember { mutableStateOf("") }
    AlertDialog(
        title = { Text(text = stringResource(id = R.string.start_game)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(id = R.string.enter_starting_money))
                TextField(
                    label = { Text(text = stringResource(id = R.string.starting_money)) },
                    value = startingMoney,
                    onValueChange = { value -> startingMoney = value.filter { it.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        onDismissRequest = onDismissButtonClicked,
        dismissButton = {
            Button(onClick = onDismissButtonClicked) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmButtonClicked(startingMoney.toIntOrNull() ?: 0) }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        })
}
