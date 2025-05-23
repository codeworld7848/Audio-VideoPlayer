package com.anand.core.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import kotlin.reflect.KType


/**
 * Extension function for creating a custom composable within a navigation graph.
 *
 * This function defines a custom composable within a navigation graph, allowing
 * for custom animations and transitions when navigating to this destination.
 *
 * @param content The content of the composable to be displayed when navigating to the specified route.
 */

inline fun <reified T : Any> NavGraphBuilder.customNavGraphComposable(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    crossinline content: @Composable (NavBackStackEntry) -> Unit
) {
    composable<T>(
        typeMap = typeMap,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(1000)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(1000)
            )
        }
    ) {
        content.invoke(it)
    }
}

/**
 * Extension property to check if the NavHostController can proceed with navigation.
 * It returns true if the current back stack entry's lifecycle state is RESUMED.
 */
val NavHostController.canProceed: Boolean
    get() = this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

/**
 * Extension function to navigate back in the navigation stack.
 * It pops the back stack up to the specified destination ID if provided.
 *
 * @param id The destination ID to pop back to. If null, it pops the current destination.
 * @param inclusive Flag indicating whether to also pop the specified destination ID.
 */
fun NavHostController.navigateBack(id: String? = null, inclusive: Boolean? = null) {
    if (canProceed) {
        if (id != null) {
            popBackStack(id, inclusive = inclusive == true)
        } else {
            popBackStack()
        }

    }
}

/**
 * Extension function to navigate to a specified destination.
 * It also provides an option to pop up to a specific destination before navigating.
 *
 * @param destination The destination to navigate to.
 */
fun <T : Any> NavHostController.navigateTo(
    destination: T,
    navOptions: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(destination) {
        navOptions.invoke(this)
    }
}

