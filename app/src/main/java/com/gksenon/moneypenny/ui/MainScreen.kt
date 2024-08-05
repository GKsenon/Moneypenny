package com.gksenon.moneypenny.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gksenon.moneypenny.R

@Composable
fun MainScreen(
    onNavigateToStartLocalGameScreen: () -> Unit,
    onNavigateToHostMultiplayerScreen: () -> Unit,
    onNavigateToJoinMultiplayerScreen: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
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
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToStartLocalGameScreen
            ) {
                Text(text = stringResource(id = R.string.start_local))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToHostMultiplayerScreen
            ) {
                Text(text = stringResource(id = R.string.host_multiplayer))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToJoinMultiplayerScreen
            ) {
                Text(text = stringResource(id = R.string.join_multiplayer))
            }
        }
    }
}
