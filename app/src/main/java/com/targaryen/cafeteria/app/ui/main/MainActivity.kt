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
import com.targaryen.cafeteria.app.navigation.CafeteriaNavDisplay
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_network.tracker.PaymentStatus
import com.targaryen.cafeteria.core_network.tracker.PaymentStatusTracker
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val paymentStatusTracker: PaymentStatusTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

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
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "cafeteria" && data.host == "checkout") {
            val status =
                when (data.lastPathSegment) {
                    "success" -> PaymentStatus.SUCCESS
                    "failure" -> PaymentStatus.FAILURE
                    "pending" -> PaymentStatus.PENDING
                    else -> PaymentStatus.CANCELLED
                }
            paymentStatusTracker.updateStatus(status)
        }
    }
}
