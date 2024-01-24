package com.gksenon.moneypenny.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gksenon.moneypenny.R
import com.gksenon.moneypenny.viewmodel.GameScreenState
import com.gksenon.moneypenny.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    when (state) {
        is GameScreenState.GameNotStarted -> GameNotStartedScreen(state = state as GameScreenState.GameNotStarted,
            onStartingMoneyChanged = { value -> viewModel.onStartingMoneyChanged(value) },
            onStartButtonClicked = { viewModel.onStartButtonClicked() })

        is GameScreenState.GameInProgress -> GameInProgressScreen(state = state as GameScreenState.GameInProgress,
            onAddButtonClicked = { viewModel.onAddButtonClicked() },
            onSubtractButtonClicked = { viewModel.onSubtractButtonClicked() },
            onAddDialogConfirmed = { viewModel.onAddDialogConfirmed() },
            onAddDialogDismissed = { viewModel.onAddDialogDismissed() },
            onSubtractDialogConfirmed = { viewModel.onSubtractDialogConfirmed() },
            onSubtractDialogDismissed = { viewModel.onSubtractDialogDismissed() },
            onMoneyValueChanged = { viewModel.onMoneyValueChanged(it) },
            onFinishGameButtonClicked = { viewModel.onFinishGameClicked() })
    }
}

@Composable
fun GameNotStartedScreen(
    state: GameScreenState.GameNotStarted,
    onStartingMoneyChanged: (String) -> Unit,
    onStartButtonClicked: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding() + 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            OutlinedTextField(
                label = {
                    val label = if (state.showStartingMoneyInvalidError)
                        R.string.starting_money_is_invalid
                    else
                        R.string.starting_money
                    Text(text = stringResource(id = label))
                },
                value = state.startingMoney,
                onValueChange = { value -> onStartingMoneyChanged(value) },
                isError = state.showStartingMoneyInvalidError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onStartButtonClicked, modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.start_game))
            }
        }
    }
}

@Composable
fun GameInProgressScreen(
    state: GameScreenState.GameInProgress,
    onAddButtonClicked: () -> Unit,
    onAddDialogConfirmed: () -> Unit,
    onAddDialogDismissed: () -> Unit,
    onSubtractButtonClicked: () -> Unit,
    onSubtractDialogConfirmed: () -> Unit,
    onSubtractDialogDismissed: () -> Unit,
    onMoneyValueChanged: (String) -> Unit,
    onFinishGameButtonClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = onFinishGameButtonClicked) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_finish_game),
                            contentDescription = stringResource(id = R.string.finish_game)
                        )
                    }
                }
            )
        }) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding() + 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            Column {
                Text(
                    text = stringResource(R.string.balance, state.balance),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color = MaterialTheme.colorScheme.outline)
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.transactionHistory) {transaction ->
                        Text(text = transaction.toString())
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color = MaterialTheme.colorScheme.outline)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Button(onClick = onAddButtonClicked) {
                        Text(text = stringResource(id = R.string.add))
                    }
                    Button(onClick = onSubtractButtonClicked) {
                        Text(text = stringResource(id = R.string.subtract))
                    }
                }
            }
        }
    }

    if (state.showAddMoneyDialog) {
        MoneyTransferDialog(
            title = stringResource(id = R.string.add),
            value = state.moneyValue,
            onValueChanged = onMoneyValueChanged,
            onDialogDismissed = onAddDialogDismissed,
            onDialogConfirmed = onAddDialogConfirmed
        )
    }

    if (state.showSubtractMoneyDialog) {
        MoneyTransferDialog(
            title = stringResource(id = R.string.subtract),
            value = state.moneyValue,
            onValueChanged = onMoneyValueChanged,
            onDialogDismissed = onSubtractDialogDismissed,
            onDialogConfirmed = onSubtractDialogConfirmed
        )
    }
}

@Composable
fun MoneyTransferDialog(
    title: String,
    value: String,
    onValueChanged: (String) -> Unit,
    onDialogDismissed: () -> Unit,
    onDialogConfirmed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDialogDismissed,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChanged,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(text = stringResource(id = R.string.amount)) }
            )
        },
        confirmButton = {
            TextButton(onClick = onDialogConfirmed) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDialogDismissed) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
    )
}
