package org.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val log = KotlinLogging.logger {}
private val t = Terminal()

class Currencies(val client: HttpClient) : CliktCommand() {
    fun fetchCurrencies(): Map<String, List<String>> = runBlocking {
        val response = client.get("https://restcountries.com/v3.1/all")
        val countries: List<Country> = response.body()
        val currencies = mutableMapOf<String, MutableList<String>>()
        for (country in countries) {
            // Only consider independent countries
            if (!country.independent) continue

            for (currency in country.currencies.keys) {
                if (!currencies.containsKey(currency)) {
                    currencies.set(currency, mutableListOf(country.name.common))
                } else {
                    currencies[currency]!!.add(country.name.common)
                }
            }
        }
        return@runBlocking currencies
    }

    fun render(currencies: Map<String, List<String>>) {
        t.println(table {
            borderStyle = rgb("#4b25b9")
            tableBorders = Borders.ALL
            header {
                style = TextStyles.bold + TextColors.brightRed
                row("Country", "Currency")
            }
            body {
                cellBorders = Borders.LEFT_RIGHT
                column(0) {
                    style = TextColors.green
                }
                column(1) {
                    style = TextColors.brightBlue
                }
                rowStyles(TextStyle(), TextStyles.dim.style)
                currencies.forEach {
                    val currency = it.key
                    it.value.forEachIndexed { i, countryName ->
                        if (i == 0) {
                            row(currency, countryName)
                        } else {
                            row(" ∟ ", countryName)
                        }
                    }
                }
            }
        })
    }

    override fun run() {
        render(fetchCurrencies())
    }
}
