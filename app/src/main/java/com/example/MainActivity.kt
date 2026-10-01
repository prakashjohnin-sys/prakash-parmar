package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.AttendanceViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.StudentDatabaseScreen
import com.example.ui.screens.TakeAttendanceScreen
import com.example.ui.screens.ViewAttendanceScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AttendanceApp()
                }
            }
        }
    }
}

@Composable
fun AttendanceApp(
    viewModel: AttendanceViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Back handling for sub-screens
    if (currentScreen != AppScreen.LOGIN && currentScreen != AppScreen.HOME) {
        BackHandler {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            AppScreen.LOGIN -> LoginScreen(viewModel = viewModel)
            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
            AppScreen.TAKE_ATTENDANCE -> TakeAttendanceScreen(viewModel = viewModel)
            AppScreen.VIEW_ATTENDANCE -> ViewAttendanceScreen(viewModel = viewModel)
            AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
            AppScreen.STUDENT_DATABASE -> StudentDatabaseScreen(viewModel = viewModel)
        }
    }
}
