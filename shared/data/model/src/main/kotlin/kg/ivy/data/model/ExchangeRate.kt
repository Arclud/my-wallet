package kg.ivy.data.model

import kg.ivy.data.model.primitive.AssetCode
import kg.ivy.data.model.primitive.PositiveDouble

data class ExchangeRate(
    val baseCurrency: AssetCode,
    val currency: AssetCode,
    val rate: PositiveDouble,
    val manualOverride: Boolean,
)
