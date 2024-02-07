package org.example

import kotlinx.serialization.Serializable

@Serializable
data class Name(
    val common: String
)

@Serializable
data class Currency(
    val name: String
)

@Serializable
data class Country(
    val name: Name,
    val currencies: Map<String, Currency> = emptyMap(),
    val independent: Boolean = true,
) {
    val currency: String?
        get() {
            if (this.currencies.isEmpty()) return null

            // Grab the first currency. There should be only one, anyway.
            return this.currencies.keys.first()
        }
}
