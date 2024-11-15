package kg.ivy.data.remote

import arrow.core.Either
import kg.ivy.data.remote.responses.ExchangeRatesResponse

interface RemoteExchangeRatesDataSource {
    suspend fun fetchEurExchangeRates(): Either<String, ExchangeRatesResponse>
}
