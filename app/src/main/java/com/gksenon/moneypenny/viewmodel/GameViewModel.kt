package com.gksenon.moneypenny.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.Player
import com.gksenon.moneypenny.domain.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class GameViewModel @Inject constructor(private val accountant: Accountant) : ViewModel() {

    private val _state = MutableStateFlow(GameScreenState())
    val state = _state.asStateFlow()

    init {
        accountant.getPlayers().onEach { players ->
            _state.update { previousState ->
                val playerCards = players.map { player ->
                    val oldCard =
                        previousState.playerCards.find { card -> card.player.id == player.id }
                    if (oldCard != null)
                        oldCard.copy(player = player)
                    else {
                        val red = Random.nextInt(256)
                        val green = Random.nextInt(256)
                        val blue = Random.nextInt(256)
                        PlayerCard(player = player, color = Triple(red, green, blue))
                    }
                }
                previousState.copy(playerCards = playerCards)
            }
        }.launchIn(viewModelScope)
        accountant.getLastTransaction().onEach { transaction ->
            _state.update { previousState -> previousState.copy(lastTransaction = transaction) }
        }.launchIn(viewModelScope)
    }

    fun onPlayerClicked(player: Player) {
        _state.update { previousState ->
            val availableRecipients = previousState.playerCards
                .map { card -> card.player }
                .minus(player)
            previousState.copy(
                moneyTransferDialogState = MoneyTransferDialogState.Opened(
                    sender = player,
                    availableRecipients = availableRecipients,
                    selectedRecipient = availableRecipients.first(),
                    amount = "",
                    isConfirmButtonEnabled = false
                )
            )
        }
    }

    fun onRecipientChanged(id: UUID) {
        _state.update { previousState ->
            val players = previousState.playerCards.map { card -> card.player }
            val dialogState =
                previousState.moneyTransferDialogState as MoneyTransferDialogState.Opened
            val recipient = players.find { player -> player.id == id } ?: players.last()
            previousState.copy(moneyTransferDialogState = dialogState.copy(selectedRecipient = recipient))
        }
    }

    fun onAmountChanged(value: String) {
        _state.update { previousState ->
            val dialogState =
                previousState.moneyTransferDialogState as MoneyTransferDialogState.Opened
            val amountValue = value.filter { it.isDigit() }.take(9)
            val parsedAmount = amountValue.toIntOrNull()
            val isAmountValid = parsedAmount != null && parsedAmount > 0
            previousState.copy(
                moneyTransferDialogState = dialogState.copy(
                    amount = amountValue,
                    isConfirmButtonEnabled = isAmountValid
                )
            )
        }
    }

    fun onMoneyTransferDialogConfirmed() {
        val dialogState =
            _state.value.moneyTransferDialogState as MoneyTransferDialogState.Opened
        val senderId = dialogState.sender.id
        val recipientId = dialogState.selectedRecipient.id
        val amount = dialogState.amount.toIntOrNull() ?: 0

        _state.update { previousState ->
            previousState.copy(moneyTransferDialogState = MoneyTransferDialogState.Closed)
        }

        viewModelScope.launch { accountant.sendMoney(senderId, recipientId, amount) }
    }

    fun onMoneyTransferDialogDismissed() {
        _state.update { previousState ->
            previousState.copy(moneyTransferDialogState = MoneyTransferDialogState.Closed)
        }
    }

    fun onCancelTransactionButtonClicked() {
        _state.update { previousState -> previousState.copy(showCancelLastTransactionConfirmation = true) }
    }

    fun onCancelLastTransactionConfirmationDialogConfirmed() {
        _state.update { previousState -> previousState.copy(showCancelLastTransactionConfirmation = false) }
        val lastTransactionId = _state.value.lastTransaction?.id
        if (lastTransactionId != null)
            viewModelScope.launch { accountant.cancelTransaction(lastTransactionId) }
    }

    fun onCancelLastTransactionConfirmationDialogDismissed() {
        _state.update { previousState -> previousState.copy(showCancelLastTransactionConfirmation = false) }
    }

    fun onFinishButtonClicked() {
        viewModelScope.launch { accountant.finishGame() }
    }
}

data class GameScreenState(
    val playerCards: List<PlayerCard> = emptyList(),
    val lastTransaction: Transaction? = null,
    val moneyTransferDialogState: MoneyTransferDialogState = MoneyTransferDialogState.Closed,
    val showCancelLastTransactionConfirmation: Boolean = false
)

data class PlayerCard(val player: Player, val color: Triple<Int, Int, Int>)

sealed class MoneyTransferDialogState {

    data object Closed : MoneyTransferDialogState()

    data class Opened(
        val sender: Player,
        val availableRecipients: List<Player>,
        val selectedRecipient: Player,
        val amount: String,
        val isConfirmButtonEnabled: Boolean
    ) : MoneyTransferDialogState()
}
