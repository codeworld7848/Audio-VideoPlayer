package com.anand.core.utils

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * CustomNavType is a generic navigation argument type that allows complex objects
 * to be passed as navigation arguments using Kotlin Serialization.
 *
 * @param T The type of object being passed.
 * @param serializer The Kotlin Serialization serializer for the object.
 * @param isNullableAllowed Determines whether null values are allowed.
 */
class CustomNavType<T : Any>(
    private val serializer: KSerializer<T>,
    override val isNullableAllowed: Boolean = false
) : NavType<T>(isNullableAllowed) {

    /**
     * Retrieves the serialized object from the bundle.
     * @param bundle The navigation arguments bundle.
     * @param key The key associated with the argument.
     * @return The deserialized object of type T or null if not found.
     */
    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let {
            val decodedValue = Uri.decode(it) // Decode URI-encoded string
            Json.decodeFromString(serializer, decodedValue) // Deserialize JSON to object
        }

    /**
     * Stores the serialized object into the bundle.
     * @param bundle The navigation arguments bundle.
     * @param key The key to associate with the argument.
     * @param value The object to be serialized and stored.
     */
    override fun put(bundle: Bundle, key: String, value: T) =
        bundle.putString(key, serializeAsValue(value)) // Store as a JSON-encoded string

    /**
     * Parses a URI-encoded JSON string into an object of type T.
     * @param value The URI-encoded JSON string.
     * @return The deserialized object.
     */
    override fun parseValue(value: String): T {
        val decodedValue = Uri.decode(value)
        return Json.decodeFromString(serializer, decodedValue)
    }

    /**
     * Serializes an object of type T into a URI-encoded JSON string.
     * @param value The object to serialize.
     * @return The serialized JSON string.
     */
    override fun serializeAsValue(value: T): String {
        val jsonString = Json.encodeToString(serializer, value) // Convert object to JSON
        return Uri.encode(jsonString) // Encode to make it URI-safe
    }

    /**
     * The name of the navigation argument type, derived from the serializer descriptor.
     */
    @OptIn(ExperimentalSerializationApi::class)
    override val name: String = serializer.descriptor.serialName
}