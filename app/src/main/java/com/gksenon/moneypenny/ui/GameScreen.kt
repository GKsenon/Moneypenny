package com.gksenon.moneypenny.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
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
import com.gksenon.moneypenny.domain.Player
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
        Column(
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
                modifier = Modifier.weight(1f)
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
                        modifier = Modifier.aspectRatio(1f)
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
            Button(
                onClick = { viewModel.onSendMoneyButtonClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.send_money))
            }
        }
    }

    val dialogState = state.moneyTransferDialogState
    if (dialogState is MoneyTransferDialogState.Opened) {
        SendMoneyDialog(
            players = state.playerCards.map { card -> card.player },
            sender = dialogState.sender,
            onSenderChanged = { id -> viewModel.onSenderChanged(id) },
            recipient = dialogState.recipient,
            onRecipientChanged = { id -> viewModel.onRecipientChanged(id) },
            amount = dialogState.amount,
            onAmountChanged = { value -> viewModel.onAmountChanged(value) },
            onDialogConfirmed = { viewModel.onMoneyTransferDialogConfirmed() },
            onDialogDismissed = { viewModel.onMoneyTransferDialogDismissed() }
        )
    }
}

@Composable
fun SendMoneyDialog(
    players: List<Player>,
    sender: Player,
    onSenderChanged: (UUID) -> Unit,
    recipient: Player,
    onRecipientChanged: (UUID) -> Unit,
    amount: String,
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
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectorOptions =
                        players.map { player -> Option(id = player.id, text = player.name) }
                    Selector(
                        options = selectorOptions,
                        selected = Option(id = sender.id, text = sender.name),
                        onOptionSelected = onSenderChanged,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = stringResource(id = R.string.send_money)
                    )
                    Selector(
                        options = selectorOptions,
                        selected = Option(id = recipient.id, text = recipient.name),
                        onOptionSelected = onRecipientChanged,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChanged,
                    label = { Text(text = stringResource(id = R.string.amount_of_money)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
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
        }
    )
}
