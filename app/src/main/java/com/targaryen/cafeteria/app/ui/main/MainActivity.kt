package com.targaryen.cafeteria.app.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.targaryen.cafeteria.app.navigation.CafeteriaIntentRouter
import com.targaryen.cafeteria.app.navigation.CafeteriaNavDisplay
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val intentRouter: CafeteriaIntentRouter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intentRouter.routeIntent(intent, lifecycleScope)

        enableEdgeToEdge()
        setContent {
            TargaryenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CafeteriaNavDisplay()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentRouter.routeIntent(intent, lifecycleScope)
    }
}
