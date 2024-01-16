package com.gksenon.moneypenny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gksenon.moneypenny.ui.GameScreen
import com.gksenon.moneypenny.ui.theme.MoneypennyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneypennyTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "game") {
        composable(route = "game") {
            GameScreen()
        }
    }
}