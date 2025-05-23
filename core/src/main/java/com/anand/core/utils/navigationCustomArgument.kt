package com.anand.core.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Creates a custom navigation argument type for passing complex objects in Jetpack Compose navigation.
 *
 * This function utilizes Kotlin's reified type parameters and reflection to generate a `CustomNavType`
 * for the specified type, allowing complex data to be safely passed between destinations.
 *
 * @param T The type of object being passed.
 * @param isNullable Determines whether the argument can be null.
 * @return A pair consisting of the KType of T and a corresponding CustomNavType instance.
 */
inline fun <reified T : Any> navigationCustomArgument(isNullable: Boolean = false): Pair<KType, CustomNavType<T>> {
    val serializer: KSerializer<T> = serializer() // Retrieve the serializer for the given type
    return typeOf<T>() to CustomNavType(serializer, isNullable) // Return the type and custom NavType
}
