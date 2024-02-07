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

class EuropeTest {
    private val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))

    private val responseContent = Json.encodeToString(
        listOf(
            Country(Name(common = "FooLand"), currencies = mapOf("FOO" to Currency("Foo"))),
            Country(Name(common = "BarLand"), currencies = mapOf("BAR" to Currency("Bar"))),
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

    private val europe = Europe(testClient)

    @Test
    fun `fetch countries without sorting`() = runBlocking {
        val countries = europe.fetchCountriesInEurope(null)

        assertEquals(2, countries.size)

        assertEquals("FooLand", countries[0].name.common)
        assertEquals(1, countries[0].currencies.size)
        assertEquals("Foo", countries[0].currencies["FOO"]?.name)

        assertEquals("BarLand", countries[1].name.common)
        assertEquals(1, countries[1].currencies.size)
        assertEquals("Bar", countries[1].currencies["BAR"]?.name)
    }

    @Test
    fun `fetch countries with sorting`() {
        val countries = europe.fetchCountriesInEurope("name:asc")

        assertEquals(2, countries.size)

        assertEquals("BarLand", countries[0].name.common)
        assertEquals("FooLand", countries[1].name.common)
    }
}
