package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import com.example.ui.AppNavHost
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.util.Haptics.init(this)
        enableEdgeToEdge()
        intent?.let { handleIntent(it) }
        setContent {
            val useAccentColors = viewModel.settingsRepo.useAccentColors.collectAsState(initial = true).value

            MyApplicationTheme(useAccentColors = useAccentColors) {
                AppNavHost(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent) {
        val launchedFromNotification = intent.getBooleanExtra("launched_from_notification", false)
        if (launchedFromNotification) {
            viewModel.triggerPromptReveal()
        }
    }
}
