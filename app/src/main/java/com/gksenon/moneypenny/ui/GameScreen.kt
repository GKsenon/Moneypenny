package com.gksenon.moneypenny.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gksenon.moneypenny.R
import com.gksenon.moneypenny.viewmodel.GameViewModel
import com.gksenon.moneypenny.viewmodel.MoneyTransferDialogState
import java.util.UUID

@Composable
fun GameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = { viewModel.onFinishButtonClicked() }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_finish_game),
                            contentDescription = stringResource(id = R.string.finish_game)
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.padding(
                top = contentPadding.calculateTopPadding() + 16.dp,
                start = 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
                end = 16.dp
            )
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(count = 2),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.playerCards) { playerCard ->
                    val backgroundColor = Color(
                        red = playerCard.color.first,
                        green = playerCard.color.second,
                        blue = playerCard.color.third
                    )
                    val textColor =
                        if (backgroundColor.luminance() > 0.5) Color.Black else Color.White
                    Card(
                        colors = cardColors(containerColor = backgroundColor),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { viewModel.onPlayerClicked(playerCard.player) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            val balance =
                                if (playerCard.player.balance < Int.MAX_VALUE) "${playerCard.player.balance} \$" else "∞"
                            Text(
                                text = playerCard.player.name,
                                color = textColor,
                                fontSize = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = balance,
                                color = textColor,
                                fontSize = 28.sp,
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    }
                }
            }
            val lastTransaction = state.lastTransaction
            if (lastTransaction != null) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "${lastTransaction.sender.name} -> ${lastTransaction.recipient.name}",
                                fontSize = 20.sp
                            )
                            Text(
                                text = "${lastTransaction.amount} \$",
                                fontSize = 24.sp
                            )
                        }
                        IconButton(onClick = { viewModel.onCancelTransactionButtonClicked() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_undo),
                                contentDescription = stringResource(id = R.string.cancel_transaction),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    val dialogState = state.moneyTransferDialogState
    if (dialogState is MoneyTransferDialogState.Opened) {
        SendMoneyDialog(
            state = dialogState,
            onRecipientChanged = { id -> viewModel.onRecipientChanged(id) },
            onAmountChanged = { value -> viewModel.onAmountChanged(value) },
            onDialogConfirmed = { viewModel.onMoneyTransferDialogConfirmed() },
            onDialogDismissed = { viewModel.onMoneyTransferDialogDismissed() }
        )
    }
}

@Composable
fun SendMoneyDialog(
    state: MoneyTransferDialogState.Opened,
    onRecipientChanged: (UUID) -> Unit,
    onAmountChanged: (String) -> Unit,
    onDialogConfirmed: () -> Unit,
    onDialogDismissed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDialogDismissed,
        title = { Text(text = stringResource(id = R.string.send_money)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectorOptions =
                        state.availableRecipients.map { player ->
                            Option(
                                id = player.id,
                                text = player.name
                            )
                        }
                    Text(
                        text = state.sender.name
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = stringResource(id = R.string.send_money)
                    )
                    Selector(
                        options = selectorOptions,
                        selected = Option(
                            id = state.selectedRecipient.id,
                            text = state.selectedRecipient.name
                        ),
                        onOptionSelected = onRecipientChanged,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = onAmountChanged,
                    label = { Text(text = stringResource(id = R.string.amount_of_money)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDialogConfirmed, enabled = state.isConfirmButtonEnabled) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDialogDismissed) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        }
    )
}
