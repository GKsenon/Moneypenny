package com.gksenon.moneypenny.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gksenon.moneypenny.R
import com.gksenon.moneypenny.viewmodel.JoinMultiplayerScreenState
import com.gksenon.moneypenny.viewmodel.JoinMultiplayerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun JoinMultiplayerScreen(
    viewModel: JoinMultiplayerViewModel = hiltViewModel(),
    onNavigateToGameScreen: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.join_multiplayer)) }) }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding() + 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            val permissions = buildList {
                add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                add(android.Manifest.permission.ACCESS_FINE_LOCATION)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
                    add(android.Manifest.permission.BLUETOOTH_CONNECT)
                    add(android.Manifest.permission.BLUETOOTH_SCAN)
                }
            }
            val permissionState = rememberMultiplePermissionsState(permissions)
            if (!permissionState.allPermissionsGranted) {
                PermissionsRequiredScreen(requestPermissions = { permissionState.launchMultiplePermissionRequest() })
            } else {
                val state by viewModel.state.collectAsState()
                when (state) {
                    is JoinMultiplayerScreenState.PlayerNameRequested ->
                        PlayerNameRequestedScreen(
                            state = state as JoinMultiplayerScreenState.PlayerNameRequested,
                            onNameChanged = { viewModel.onNameChanged(it) },
                            onNameConfirmed = { viewModel.onNameConfirmed() }
                        )

                    JoinMultiplayerScreenState.DiscoveryStarted -> DiscoveryScreen()
                    JoinMultiplayerScreenState.ConnectingToHost -> ConnectingScreen()
                    JoinMultiplayerScreenState.AcceptedByHost -> AcceptedScreen()
                    JoinMultiplayerScreenState.RejectedByHost -> RejectedScreen(
                        onTryAgainButtonClicked = { viewModel.onTryAgainButtonClicked() })
                    JoinMultiplayerScreenState.GameStarted -> LaunchedEffect(state) { onNavigateToGameScreen() }
                }
            }
        }
    }
}

@Composable
fun PermissionsRequiredScreen(requestPermissions: () -> Unit) {
    Text(
        text = stringResource(id = R.string.join_multiplayer_permission_request_rationale),
        textAlign = TextAlign.Center
    )
    Button(onClick = requestPermissions) {
        Text(text = stringResource(id = R.string.request_permissions))
    }
}

@Composable
fun PlayerNameRequestedScreen(
    state: JoinMultiplayerScreenState.PlayerNameRequested,
    onNameChanged: (String) -> Unit,
    onNameConfirmed: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChanged,
            label = { Text(text = stringResource(id = R.string.your_name)) },
            modifier = Modifier.weight(1f)
        )
        FilledIconButton(
            onClick = onNameConfirmed,
            enabled = state.isConfirmButtonEnabled
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(id = R.string.connect_to_host)
            )
        }
    }
}

@Composable
fun DiscoveryScreen() {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = stringResource(id = R.string.discovery_started))
}

@Composable
fun ConnectingScreen() {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = stringResource(id = R.string.connecting))
}

@Composable
fun AcceptedScreen() {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = stringResource(id = R.string.connection_accepted))
}

@Composable
fun RejectedScreen(onTryAgainButtonClicked: () -> Unit) {
    Text(text = stringResource(id = R.string.connection_rejected))
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = onTryAgainButtonClicked) {
        Text(text = stringResource(id = R.string.try_again))
    }
}
