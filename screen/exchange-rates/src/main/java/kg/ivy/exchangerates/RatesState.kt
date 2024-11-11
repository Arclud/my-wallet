package kg.ivy.exchangerates

import kg.ivy.exchangerates.data.RateUi
import kotlinx.collections.immutable.ImmutableList

data class RatesState(
    val baseCurrency: String,
    val manual: ImmutableList<RateUi>,
    val automatic: ImmutableList<RateUi>
)
