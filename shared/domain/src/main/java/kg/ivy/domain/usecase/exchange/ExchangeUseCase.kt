package kg.ivy.domain.usecase.exchange

import arrow.core.Option
import kg.ivy.data.model.PositiveValue
import kg.ivy.data.model.Value
import kg.ivy.data.model.primitive.AssetCode
import kg.ivy.data.model.primitive.NonZeroDouble
import kg.ivy.data.repository.ExchangeRatesRepository
import javax.inject.Inject

@Suppress("UnusedPrivateProperty", "UnusedParameter")
class ExchangeUseCase @Inject constructor(
    private val exchangeRatesRepository: ExchangeRatesRepository,
) {
    /**
     * Converts a [value] from one currency [to] another.
     *
     * @return some value if the conversion was successful or [arrow.core.None]
     */
    suspend fun convert(value: PositiveValue, to: AssetCode): Option<PositiveValue> {
        TODO("Not implemented")
    }

    /**
     * @return none exchanged value for empty [values] or if the
     * exchange fails for all [AssetCode]
     */
    suspend fun convert(
        values: Map<AssetCode, NonZeroDouble>,
        to: AssetCode
    ): ExchangeResult {
        TODO("Not implemented")
    }

    /**
     * Converts a [value] from one currency [to] another.
     *
     * @return some value if the conversion was successful or [arrow.core.None]
     */
    suspend fun convert(
        value: Value,
        to: AssetCode
    ): Option<Value> {
        TODO("Not implemented")
    }
}

data class ExchangeResult(
    /**
     * Some value that was exchanged successfully.
     * Or [arrow.core.None] if there were errors or an empty map was exchanged.
     */
    val exchanged: Option<Value>,
    /**
     * Assets for which the exchange has failed.
     */
    val exchangeErrors: Set<AssetCode>,
)