package com.gksenon.moneypenny.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gksenon.moneypenny.R
import com.gksenon.moneypenny.viewmodel.StartViewModel

@Composable
fun StartScreen(viewModel: StartViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            val startingMoneyLabel = stringResource(id = R.string.starting_money)
            OutlinedTextField(
                value = state.startingMoney,
                onValueChange = { viewModel.onStartingMoneyChanged(it) },
                label = { Text(text = startingMoneyLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = startingMoneyLabel }
            )
            val playerNameLabel = stringResource(id = R.string.player)
            OutlinedTextField(
                value = state.playerName,
                onValueChange = { viewModel.onPlayerNameChanged(it) },
                label = { Text(text = playerNameLabel) },
                trailingIcon = {
                    IconButton(onClick = { viewModel.onAddPlayerButtonClicked() }) {
                        Image(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(id = R.string.add_player)
                        )
                    }
                },
                isError = state.showPlayerNameIsEmptyError || state.showPlayerNameMustBeUniqueError,
                supportingText = {
                    if (state.showPlayerNameIsEmptyError)
                        Text(text = stringResource(id = R.string.player_name_is_empty))
                    else if (state.showPlayerNameMustBeUniqueError)
                        Text(text = stringResource(id = R.string.player_name_must_be_unique))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = playerNameLabel }
            )
            val playerNamesListContentDescription = stringResource(id = R.string.players_names)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = playerNamesListContentDescription }
            ) {
                items(state.players) { playerName ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().testTag(playerName)
                    ) {
                        Text(
                            text = playerName,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        IconButton(onClick = { viewModel.onDeletePlayerButtonClicked(playerName) }) {
                            Image(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(id = R.string.delete_player),
                                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.outline),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Button(
                onClick = { viewModel.onStartButtonClicked() },
                enabled = state.isStartButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.start_game))
            }
        }
    }
}