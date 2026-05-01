package es.guardianos.senior.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import es.guardianos.senior.ui.theme.GuardianOSSeniorTheme
import es.guardianos.senior.ui.screens.SafeInboxScreen

class SafeInboxActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuardianOSSeniorTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SafeInboxScreen()
                }
            }
        }
    }
}
