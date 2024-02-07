package org.example

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class CurrenciesTest {
    private val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))

    private val responseContent = Json.encodeToString(
        listOf(
            Country(Name(common = "FooLand"), currencies = mapOf("FOO" to Currency("Foo"))),
            Country(Name(common = "BarLand"), currencies = mapOf(
                "BAR" to Currency("Bar"),
                "FOO" to Currency("Foo"),
            )),
        )
    )

    private val testClient = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                respond(responseContent, HttpStatusCode.OK, responseHeaders)
            }
        }
        install(ContentNegotiation) {
            json()
        }
    }

    private val currencies = Currencies(testClient)

    @Test
    fun `fetch currencies`() = runBlocking {
        val result = currencies.fetchCurrencies()

        assertEquals(2, result.size)

        assertTrue(result.containsKey("FOO"))
        assertEquals(result["FOO"], listOf("FooLand", "BarLand"))

        assertTrue(result.containsKey("BAR"))
        assertEquals(result["BAR"], listOf("BarLand"))
    }
}
