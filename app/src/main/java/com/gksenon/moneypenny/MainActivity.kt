package com.gksenon.moneypenny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gksenon.moneypenny.domain.LOCAL_GAME
import com.gksenon.moneypenny.domain.MULTIPLAYER_CLIENT_GAME
import com.gksenon.moneypenny.domain.MULTIPLAYER_HOST_GAME
import com.gksenon.moneypenny.ui.GameScreen
import com.gksenon.moneypenny.ui.HostMultiplayerScreen
import com.gksenon.moneypenny.ui.JoinMultiplayerScreen
import com.gksenon.moneypenny.ui.MainScreen
import com.gksenon.moneypenny.ui.StartScreen
import com.gksenon.moneypenny.ui.theme.MoneypennyTheme
import com.gksenon.moneypenny.viewmodel.GAME_TYPE_KEY
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint

private const val MAIN_SCREEN = "main"
private const val START_LOCAL_GAME_SCREEN = "start_local"
private const val HOST_MULTIPLAYER_SCREEN = "start_multiplayer"
private const val JOIN_MULTIPLAYER_SCREEN = "join_multiplayer"
private const val GAME_SCREEN = "game"

@AndroidEntryPoint
@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneypennyTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = MAIN_SCREEN) {
                    composable(route = MAIN_SCREEN) {
                        MainScreen(
                            onNavigateToStartLocalGameScreen = {
                                navController.navigate(START_LOCAL_GAME_SCREEN)
                            },
                            onNavigateToHostMultiplayerScreen = {
                                navController.navigate(HOST_MULTIPLAYER_SCREEN)
                            },
                            onNavigateToJoinMultiplayerScreen = {
                                navController.navigate(JOIN_MULTIPLAYER_SCREEN)
                            }
                        )
                    }
                    composable(route = START_LOCAL_GAME_SCREEN) {
                        StartScreen(onNavigateToGameScreen = {
                            val options = NavOptions.Builder()
                                .setPopUpTo(route = MAIN_SCREEN, inclusive = false)
                                .build()
                            navController.navigate(route = "$GAME_SCREEN/$LOCAL_GAME", navOptions = options)
                        })
                    }
                    composable(route = HOST_MULTIPLAYER_SCREEN) {
                        HostMultiplayerScreen(
                            onNavigateToGameScreen = {
                                val options = NavOptions.Builder()
                                    .setPopUpTo(route = MAIN_SCREEN, inclusive = false)
                                    .build()
                                navController.navigate(
                                    route = "$GAME_SCREEN/$MULTIPLAYER_HOST_GAME",
                                    navOptions = options
                                )
                            }
                        )
                    }
                    composable(route = JOIN_MULTIPLAYER_SCREEN) {
                        JoinMultiplayerScreen(onNavigateToGameScreen = {
                            val options = NavOptions.Builder()
                                .setPopUpTo(route = MAIN_SCREEN, inclusive = false)
                                .build()
                            navController.navigate(
                                route = "$GAME_SCREEN/$MULTIPLAYER_CLIENT_GAME",
                                navOptions = options
                            )
                        })
                    }
                    composable(
                        route = "$GAME_SCREEN/{$GAME_TYPE_KEY}",
                        arguments = listOf(navArgument(GAME_TYPE_KEY) { type = NavType.StringType })
                    ) {
                        GameScreen(onNavigateToMainScreen = {
                            navController.popBackStack()
                        })
                    }
                }
            }
        }
    }
}
