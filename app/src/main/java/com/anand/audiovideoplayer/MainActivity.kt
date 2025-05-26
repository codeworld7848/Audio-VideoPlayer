package com.anand.audiovideoplayer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.anand.audiovideoplayer.navigation.SetUpNavGraph
import com.anand.audiovideoplayer.ui.theme.AudioVideoPlayerTheme
import com.anand.core.utils.Navigator
import com.anand.core.utils.navigateBack
import com.anand.core.utils.navigateTo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Inject dependencies using Hilt
    @Inject
    lateinit var navigator: Navigator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_AUDIO),0)
        setContent {
            val navHostController = rememberNavController()
            AudioVideoPlayerTheme {
                SetUpNavGraph(navController = navHostController)

                /**
                 * Handle navigation events, including:
                 * - Navigating back
                 * - Closing the app
                 * - Navigating to a new screen
                 */
                LaunchedEffect(key1 = true) {
                    withContext(Dispatchers.Main.immediate) {
                        navigator.actions.collect { action ->
                            when (action) {
                                is Navigator.Action.Back -> {
                                    action.resultKey?.let { key ->
                                        navHostController.previousBackStackEntry?.savedStateHandle?.set(
                                            key,
                                            action.result
                                        )
                                    }
                                    navHostController.navigateBack()
                                }

                                Navigator.Action.CloseApp -> finish()
                                is Navigator.Action.Navigate<*> -> {
                                    navHostController.navigateTo(
                                        destination = action.destination as Any,
                                        navOptions = action.navOptions
                                    )
                                }
                            }
                            navigator.clearLastAction() // Prevent unwanted replays
                        }
                    }
                }
            }
        }
    }
}
