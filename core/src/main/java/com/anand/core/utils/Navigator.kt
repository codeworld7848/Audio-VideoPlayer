package com.anand.core.utils

import androidx.compose.runtime.Stable
import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton


/**
 * A singleton navigation manager that facilitates app-wide navigation using SharedFlow.
 * This allows different parts of the app to trigger navigation events in a decoupled manner.
 */
@Stable
@Singleton
class Navigator @Inject constructor() {

    // Private mutable shared flow to emit navigation actions with a buffer capacity of 10
    private val _actions = MutableSharedFlow<Action>(
        replay = 1, // No replay of past events
        extraBufferCapacity = 10 // Buffer capacity for pending actions
    )

    // Public immutable flow of navigation actions, allowing observers to collect navigation events
    val actions: SharedFlow<Action> = _actions.asSharedFlow()

    /**
     * Navigates to a specified destination.
     * @param destinationScreen The target screen/destination.
     * @param navOptions Optional navigation options for customizing the transition.
     */
    fun <T> navigate(destinationScreen: T, navOptions: NavOptionsBuilder.() -> Unit = {}) {
        _actions.tryEmit(
            Action.Navigate(destination = destinationScreen, navOptions = navOptions)
        )
    }

    /**
     * Emits an event to close the app.
     */
    fun closeApp() {
        _actions.tryEmit(Action.CloseApp)
    }

    /**
     * Navigates back to the previous screen, optionally passing a result.
     * @param resultKey A key to identify the result.
     * @param result The result data to pass back.
     */
    fun back(resultKey: String? = null, result: Any? = null) {
        _actions.tryEmit(Action.Back(resultKey = resultKey, result = result))
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearLastAction() {
        _actions.resetReplayCache() // Clears the last emitted value
    }


    /**
     * A sealed class representing different types of navigation actions.
     */
    sealed interface Action {
        /**
         * Represents a navigation action to a specified destination.
         */
        data class Navigate<T>(
            val destination: T,
            val navOptions: NavOptionsBuilder.() -> Unit
        ) : Action

        /**
         * Represents a back navigation action, optionally carrying a result.
         */
        data class Back(
            val resultKey: String? = null,
            val result: Any? = null
        ) : Action

        /**
         * Represents an action to close the app.
         */
        data object CloseApp : Action
    }
}
