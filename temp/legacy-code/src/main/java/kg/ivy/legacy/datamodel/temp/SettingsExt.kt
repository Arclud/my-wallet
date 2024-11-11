package kg.ivy.legacy.datamodel.temp

import kg.ivy.data.db.entity.SettingsEntity
import kg.ivy.legacy.datamodel.Settings

fun SettingsEntity.toLegacyDomain(): Settings = Settings(
    theme = theme,
    baseCurrency = currency,
    bufferAmount = bufferAmount.toBigDecimal(),
    name = name,
    id = id
)
