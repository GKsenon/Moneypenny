package com.gksenon.moneypenny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gksenon.moneypenny.ui.MainScreen
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