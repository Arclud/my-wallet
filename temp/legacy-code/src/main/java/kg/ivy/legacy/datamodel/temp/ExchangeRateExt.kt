package kg.ivy.legacy.datamodel.temp

import kg.ivy.data.db.entity.ExchangeRateEntity
import kg.ivy.legacy.datamodel.ExchangeRate

fun ExchangeRateEntity.toLegacyDomain(): ExchangeRate = ExchangeRate(
    baseCurrency = baseCurrency,
    currency = currency,
    rate = rate
)
