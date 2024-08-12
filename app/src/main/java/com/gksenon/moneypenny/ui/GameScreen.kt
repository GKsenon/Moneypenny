package com.gksenon.moneypenny.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gksenon.moneypenny.R
import com.gksenon.moneypenny.domain.Player
import com.gksenon.moneypenny.domain.Transaction
import com.gksenon.moneypenny.viewmodel.GameViewModel
import com.gksenon.moneypenny.viewmodel.MoneyTransferDialogState
import com.gksenon.moneypenny.viewmodel.PlayerCard

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateToMainScreen: () -> Unit
) {
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
            PlayersGrid(
                playerCards = state.playerCards,
                onTransactionRequested = { sender, recipient ->
                    viewModel.onMoneyTransferRequested(
                        sender,
                        recipient
                    )
                }
            )
            val lastTransaction = state.lastTransaction
            if (lastTransaction != null)
                LastTransactionCard(
                    lastTransaction = lastTransaction,
                    onCancelTransactionButtonClicked = { viewModel.onCancelTransactionButtonClicked() }
                )
        }
    }

    val dialogState = state.moneyTransferDialogState
    if (dialogState is MoneyTransferDialogState.Opened) {
        SendMoneyDialog(
            state = dialogState,
            onAmountChanged = { value -> viewModel.onAmountChanged(value) },
            onDialogConfirmed = { viewModel.onMoneyTransferDialogConfirmed() },
            onDialogDismissed = { viewModel.onMoneyTransferDialogDismissed() }
        )
    }

    if (state.showCancelLastTransactionConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.onCancelLastTransactionConfirmationDialogDismissed() },
            title = { Text(text = stringResource(id = R.string.cancel_transaction)) },
            text = { Text(text = stringResource(id = R.string.confirm_transaction_cancellation)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onCancelLastTransactionConfirmationDialogConfirmed() }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCancelLastTransactionConfirmationDialogDismissed() }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            })
    }

    if (state.showFinishConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.onFinishConfirmationDialogDismissed() },
            title = { Text(text = stringResource(id = R.string.finish_game)) },
            text = { Text(text = stringResource(id = R.string.finish_game_confirmation)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onFinishConfirmationDialogConfirmed(openMainScreen = onNavigateToMainScreen) }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onFinishConfirmationDialogDismissed() }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            })
    }
}

@Composable
private fun PlayersGrid(
    playerCards: List<PlayerCard>,
    onTransactionRequested: (Player, Player) -> Unit
) {
    var gridPosition by remember { mutableStateOf(Offset.Zero) }
    var cardSize by remember { mutableStateOf(IntSize.Zero) }
    val cardPositions = remember { mutableStateMapOf<Player, Offset>() }
    var start by remember { mutableStateOf(Offset.Zero) }
    var end by remember { mutableStateOf(Offset.Zero) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                gridPosition = coordinates.positionInRoot()
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        when (event.type) {
                            PointerEventType.Press -> {
                                if (getPlayerByPosition(
                                        cardPositions,
                                        cardSize,
                                        change.position
                                    ) != null
                                ) {
                                    start = change.position
                                    change.consume()
                                }
                            }

                            PointerEventType.Move -> {
                                if (start != Offset.Zero) {
                                    end = change.position
                                    change.consume()
                                }
                            }

                            PointerEventType.Release -> {
                                if (start != Offset.Zero) {
                                    val sender = getPlayerByPosition(cardPositions, cardSize, start)
                                    val recipient =
                                        getPlayerByPosition(cardPositions, cardSize, end)
                                    if (sender != null && recipient != null && sender != recipient)
                                        onTransactionRequested(sender, recipient)
                                    change.consume()
                                }
                                start = Offset.Zero
                                end = Offset.Zero
                            }
                        }
                    }
                }
            }
            .drawWithContent {
                drawContent()
                if (start != Offset.Zero && end != Offset.Zero)
                    drawLine(
                        color = Color.Magenta,
                        start = start,
                        end = end,
                        strokeWidth = 8.dp.value
                    )
            }
    ) {
        playerCards.chunked(2).forEach { playerCardsRow ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                playerCardsRow.forEach { playerCard ->
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
                            .weight(1f)
                            .aspectRatio(1.5f)
                            .onGloballyPositioned { coordinates ->
                                cardSize = coordinates.size
                                val position = coordinates.positionInRoot() - gridPosition
                                cardPositions[playerCard.player] = position
                            }
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
                if (playerCardsRow.size % 2 != 0) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LastTransactionCard(
    lastTransaction: Transaction,
    onCancelTransactionButtonClicked: () -> Unit
) {
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
            IconButton(onClick = onCancelTransactionButtonClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_undo),
                    contentDescription = stringResource(id = R.string.cancel_transaction),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
private fun SendMoneyDialog(
    state: MoneyTransferDialogState.Opened,
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
                    Text(
                        text = state.sender.name,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = stringResource(id = R.string.send_money)
                    )
                    Text(
                        text = state.recipient.name,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
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

private fun getPlayerByPosition(
    cardPositions: Map<Player, Offset>,
    cardSize: IntSize,
    touchPosition: Offset
): Player? = cardPositions
    .toList()
    .find { (_, cardPosition) ->
        val xInRange = touchPosition.x in cardPosition.x..cardPosition.x + cardSize.width
        val yInRange = touchPosition.y in cardPosition.y..cardPosition.y + cardSize.height
        xInRange && yInRange
    }?.first
