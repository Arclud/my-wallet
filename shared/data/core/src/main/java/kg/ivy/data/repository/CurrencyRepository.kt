package kg.ivy.data.repository

import android.icu.util.Currency
import kg.ivy.base.legacy.Theme
import kg.ivy.base.threading.DispatchersProvider
import kg.ivy.data.db.dao.read.SettingsDao
import kg.ivy.data.db.dao.write.WriteSettingsDao
import kg.ivy.data.db.entity.SettingsEntity
import kg.ivy.data.model.primitive.AssetCode
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val writeSettingsDao: WriteSettingsDao,
    private val dispatchersProvider: DispatchersProvider,
) {
    companion object {
        const val FALLBACK_DEFAULT_CURRENCY = "USD"
    }

    private var baseCurrencyMemo: AssetCode? = null

    suspend fun getBaseCurrency(): AssetCode = withContext(dispatchersProvider.io) {
        val baseCurrency = baseCurrencyMemo
        if (baseCurrency != null) return@withContext baseCurrency

        val currencyCode = settingsDao.findFirstOrNull()?.currency
            ?: getDefaultFIATCurrency()?.currencyCode
        currencyCode?.let(AssetCode::from)?.getOrNull()
            ?: AssetCode.unsafe(FALLBACK_DEFAULT_CURRENCY)
    }

    private fun getDefaultFIATCurrency(): Currency? {
        return Currency.getInstance(Locale.getDefault())
    }

    suspend fun setBaseBaseCurrency(newCurrency: AssetCode) {
        withContext(dispatchersProvider.io) {
            val currentEntity = settingsDao.findFirstOrNull()
                ?: SettingsEntity(
                    theme = Theme.AUTO,
                    currency = FALLBACK_DEFAULT_CURRENCY,
                    bufferAmount = 0.0,
                    name = "",
                    isSynced = true,
                    isDeleted = false,
                    id = UUID.randomUUID()
                )
            baseCurrencyMemo = newCurrency
            writeSettingsDao.save(
                currentEntity.copy(
                    currency = newCurrency.code
                )
            )
        }
    }
}