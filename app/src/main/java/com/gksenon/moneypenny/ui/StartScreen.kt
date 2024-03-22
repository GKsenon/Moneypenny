package com.gksenon.moneypenny.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gksenon.moneypenny.R
//import com.gksenon.moneypenny.viewmodel.MainScreenState
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
            OutlinedTextField(
                value = state.startingMoney,
                onValueChange = { viewModel.onStartingMoneyChanged(it) },
                label = {
                    val label = if (state.showStartingMoneyInvalidError)
                        R.string.starting_money_is_invalid
                    else
                        R.string.starting_money
                    Text(text = stringResource(id = label))
                },
                isError = state.showStartingMoneyInvalidError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.playerName,
                onValueChange = { viewModel.onPlayerNameChanged(it) },
                label = { Text(text = stringResource(id = R.string.add_player)) },
                trailingIcon = {
                    IconButton(onClick = { viewModel.onAddPlayerButtonClicked() }) {
                        Image(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(id = R.string.add_player)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.players) {
                    Text(text = it)
                }
            }
            Button(
                onClick = { viewModel.onStartButtonClicked() }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.start_game))
            }
        }
    }
}