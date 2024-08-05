package com.gksenon.moneypenny

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.ui.StartScreen
import com.gksenon.moneypenny.ui.theme.MoneypennyTheme
import com.gksenon.moneypenny.viewmodel.StartLocalGameViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val PLAYER_1 = "Player 1"
private const val PLAYER_2 = "Player 2"

class StartScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val accountantGateway = InMemoryAccountantGateway()

    private lateinit var startingMoneyTextField: SemanticsNodeInteraction
    private lateinit var playerNameTextField: SemanticsNodeInteraction
    private lateinit var addPlayerButton: SemanticsNodeInteraction
    private lateinit var playersList: SemanticsNodeInteraction
    private lateinit var startButton: SemanticsNodeInteraction
    private lateinit var nameIsEmptyError: String
    private lateinit var nameIsDuplicateError: String
    private lateinit var deletePlayerButtonContentDescription: String
    private lateinit var finishGameButtonContentDescription: String

    @Before
    fun before() {
        composeTestRule.setContent {
            MoneypennyTheme {
                StartScreen(viewModel = StartLocalGameViewModel(Accountant(accountantGateway)))
            }
            val startingMoneyContentDescription = stringResource(R.string.starting_money)
            val playerNameContentDescription = stringResource(R.string.player)
            val addPlayerButtonContentDescription = stringResource(R.string.add_player)
            val playersListContentDescription = stringResource(R.string.players_names_list_content_description)
            val startButtonText = stringResource(R.string.start_local)
            nameIsEmptyError = stringResource(id = R.string.player_name_is_empty)
            nameIsDuplicateError = stringResource(id = R.string.player_name_must_be_unique)
            deletePlayerButtonContentDescription = stringResource(id = R.string.delete_player)
            finishGameButtonContentDescription = stringResource(id = R.string.finish_game)
            startingMoneyTextField =
                composeTestRule.onNodeWithContentDescription(startingMoneyContentDescription)
            playerNameTextField =
                composeTestRule.onNodeWithContentDescription(playerNameContentDescription)
            addPlayerButton =
                composeTestRule.onNodeWithContentDescription(addPlayerButtonContentDescription)
            playersList =
                composeTestRule.onNodeWithContentDescription(playersListContentDescription)
            startButton = composeTestRule.onNodeWithText(startButtonText)
        }
    }

    @Test
    fun startUp_showsStartScreen() {
        startingMoneyTextField.assertIsDisplayed()
        playerNameTextField.assertIsDisplayed()
        addPlayerButton.assertIsDisplayed()
        startButton.assertIsDisplayed()
        startButton.assertIsNotEnabled()
    }

    @Test
    fun onStartingMoneyTextFieldChanged_validatesValue() {
        startingMoneyTextField.performTextInput("/.,+-100fg0")

        startingMoneyTextField.assert(hasText("1000"))
    }

    @Test
    fun onAddPlayerButtonClicked_ifPlayerNameIsEmpty_showsError() {
        playerNameTextField.performTextInput("")
        addPlayerButton.performClick()

        playerNameTextField.assert(hasText(nameIsEmptyError))
    }

    @Test
    fun onAddPlayerButtonClicked_ifNameIsDuplicate_showsError() {
        playerNameTextField.performTextInput(PLAYER_1)
        addPlayerButton.performClick()
        playerNameTextField.performTextInput(PLAYER_1)
        addPlayerButton.performClick()

        playerNameTextField.assert(hasText(nameIsDuplicateError))
    }

    @Test
    fun onAddButtonClicked_addsPlayer() {
        playerNameTextField.performTextInput(PLAYER_1)
        addPlayerButton.performClick()

        playersList.onChildren()
            .filter(hasTestTag(PLAYER_1))
            .assertCountEquals(1)
    }

    @Test
    fun onDeletePlayerButtonClicked_deletesPlayer() {
        playerNameTextField.performTextInput(PLAYER_1)
        addPlayerButton.performClick()

        playersList.onChildren()
            .filter(hasTestTag(PLAYER_1))
            .assertCountEquals(1)

        playersList.onChildren()
            .filter(hasTestTag(PLAYER_1))
            .onFirst()
            .onChildren()
            .filter(hasContentDescription(deletePlayerButtonContentDescription))
            .onFirst()
            .performClick()

        playersList.onChildren()
            .filter(hasTestTag(PLAYER_1))
            .assertCountEquals(1)
            .onFirst()
            .assertIsNotDisplayed()
    }

    @Test
    fun onStartingMoneyTextFieldChanged_ifValueIsInvalid_disablesStartButton() {
        startingMoneyTextField.performTextInput("0gh/*-+00")
        playerNameTextField.performTextInput(PLAYER_1)
        addPlayerButton.performClick()
        playerNameTextField.performTextInput(PLAYER_2)
        addPlayerButton.performClick()

        startButton.assertIsNotEnabled()
    }

    @Test
    fun onAddPlayerButtonClicked_ifPlayersAmountIsLessThan2_disablesStartButton() {
        startingMoneyTextField.performTextInput("1000")
        playerNameTextField.performTextInput(PLAYER_1)
        addPlayerButton.performClick()

        startButton.assertIsNotEnabled()
    }

    @Test
    fun onAddPlayerButtonClicked_ifPlayersAmountIsMoreThan8_disablesStartButton() {
        startingMoneyTextField.performTextInput("1000")
        playerNameTextField.performTextInput("PLAYER_1")
        addPlayerButton.performClick()
        playerNameTextField.performTextInput("PLAYER_2")
        addPlayerButton.performClick()
        playerNameTextField.performTextInput("PLAYER_3")
        addPlayerButton.performClick()
        playerNameTextField.performTextInput("PLAYER_4")
        addPlayerButton.performClick()
        playerNameTextField.performTextInput("PLAYER_5")
        addPlayerButton.performClick()
        playerNameTextField.performTextInput("PLAYER_6")
        addPlayerButton.performClick()
        playerNameTextField.performTextInput("PLAYER_7")
        addPlayerButton.performClick()
        playerNameTextField.performTextInput("PLAYER_8")
        addPlayerButton.performClick()
        playerNameTextField.performTextInput("PLAYER_9")
        addPlayerButton.performClick()

        startButton.assertIsNotEnabled()
    }

    @Test
    fun onStartButtonClicked_startsGame() {
        startingMoneyTextField.performTextInput("2000")
        playerNameTextField.performTextInput(PLAYER_1)
        addPlayerButton.performClick()
        playerNameTextField.performTextInput(PLAYER_2)
        addPlayerButton.performClick()
        startButton.performClick()

        runTest {
            assertEquals(2000, accountantGateway.getStartingMoney())
            assertEquals(
                listOf("Bank", PLAYER_1, PLAYER_2),
                accountantGateway.getPlayers().first().map { it.name }
            )
        }
    }
}