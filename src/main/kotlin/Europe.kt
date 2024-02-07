package org.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
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

class Europe(val client: HttpClient) : CliktCommand() {
    val sort by option().choice("name:asc", "name:desc", "currency:asc", "currency:desc")

    fun fetchCountriesInEurope(sort: String?): List<Country> = runBlocking {
        val response = client.get("https://restcountries.com/v3.1/region/europe")
        val countries: List<Country> = response.body()

        val filteredCountries = countries.filter {it.independent }

        when (sort) {
            "name:asc" -> filteredCountries.sortedBy { it.name.common }
            "name:desc" -> filteredCountries.sortedBy { it.name.common }.reversed()
            "currency:asc" -> filteredCountries.sortedBy { it.currency }
            "currency:desc" -> filteredCountries.sortedBy { it.currency }.reversed()
            else -> filteredCountries
        }
    }

    fun render(countries: List<Country>) {
        t.println(table {
            borderStyle = rgb("#4b25b9")
            tableBorders = Borders.ALL
            header {
                style = TextStyles.bold.style
                when (sort) {
                    "name:asc" -> row("${brightRed("Country")} ${brightYellow("▲")}", "${brightRed("Currency")}")
                    "name:desc" -> row("${brightRed("Country")} ${brightYellow("▼")}", "${brightRed("Currency")}")
                    "currency:asc" -> row("${brightRed("Country")}", "${brightRed("Currency")} ${brightYellow("▲")}")
                    "currency:desc" -> row("${brightRed("Country")}", "${brightRed("Currency")} ${brightYellow("▼")}")
                    else -> row("${brightRed("Country")}", "${brightRed("Currency")}")
                }
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
                for (country in countries) {
                    // Log an error if there are multiple currencies for the country. There should not
                    // be for any of the independent countries, but this could change in the future
                    if (country.currencies.size > 1) {
                        log.error("Country ${country.name.common} has multiple currencies; ${country.currencies}")
                    }

                    row(country.name.common, country.currency)
                }
            }
        })
    }

    override fun run() {
        render(fetchCountriesInEurope(sort))
    }
}
