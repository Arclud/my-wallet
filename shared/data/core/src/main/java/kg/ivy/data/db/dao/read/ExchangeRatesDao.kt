package kg.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import kg.ivy.data.db.entity.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRatesDao {
    @Query("SELECT * FROM exchange_rates")
    fun findAll(): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates WHERE manualOverride = 1")
    suspend fun findAllManuallyOverridden(): List<ExchangeRateEntity>

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :baseCurrency AND currency = :currency")
    suspend fun findByBaseCurrencyAndCurrency(
        baseCurrency: String,
        currency: String
    ): ExchangeRateEntity?
}
