package com.gksenon.moneypenny.ui

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gksenon.moneypenny.R
import com.gksenon.moneypenny.domain.HostMatchMaker
import com.gksenon.moneypenny.viewmodel.ClientConnectionStatus
import com.gksenon.moneypenny.viewmodel.HostMultiplayerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@ExperimentalPermissionsApi
@Composable
fun HostMultiplayerScreen(viewModel: HostMultiplayerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.host_multiplayer)) }) }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            OutlinedTextField(
                value = state.hostName,
                onValueChange = { value -> viewModel.onHostNameChanged(value) },
                label = { Text(text = stringResource(id = R.string.your_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.startingMoney,
                onValueChange = { value -> viewModel.onStartingMoneyChanged(value) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(text = stringResource(id = R.string.starting_money)) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(text = stringResource(id = R.string.connection_requests))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.connections) { connection ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                    ) {
                        Text(
                            text = connection.name,
                            modifier = Modifier.weight(1f)
                        )
                        if(connection.status == ClientConnectionStatus.PENDING) {
                            IconButton(onClick = { viewModel.onAcceptPlayerButtonClicked(connection.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = stringResource(id = R.string.accept_player_content_description)
                                )
                            }
                            IconButton(onClick = { viewModel.onDenyPlayerButtonClicked(connection.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(id = R.string.deny_player_content_description)
                                )
                            }
                        } else {
                            Text(text = stringResource(id = R.string.player_accepted))
                        }
                    }
                }
            }
            Button(
                onClick = { viewModel.onStartButtonClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.start))
            }
        }
    }
    if (state.showStartGameConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onStartGameConfirmationDialogDismissed() },
            title = { Text(text = stringResource(id = R.string.start_game_dialog_title)) },
            text = { Text(text = stringResource(id = R.string.start_game_dialog_text)) },
            confirmButton = {
                Button(onClick = { viewModel.onStartGameConfirmationDialogConfirmed() }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.onStartGameConfirmationDialogDismissed() }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            }
        )
    }
    val permissions = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    if (!permissions.status.isGranted) {
        LaunchedEffect(permissions) { permissions.launchPermissionRequest() }
    }
}