package kg.ivy.domain.usecase.wallet

import arrow.core.Option
import kg.ivy.data.model.Value
import kg.ivy.data.model.primitive.AssetCode
import kg.ivy.data.model.primitive.NonZeroDouble
import kg.ivy.data.repository.AccountRepository
import kg.ivy.domain.usecase.account.AccountBalanceUseCase
import kg.ivy.domain.usecase.exchange.ExchangeUseCase
import javax.inject.Inject

@Suppress("UnusedPrivateProperty", "UnusedParameter")
class WalletBalanceUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val accountBalanceUseCase: AccountBalanceUseCase,
    private val exchangeUseCase: ExchangeUseCase,
) {

    /**
     * Calculates the all-time balance of Ivy Wallet by summing
     * the balances of all included (not excluded) accounts.
     * The balance can be negative. Balances that can't be exchanged in [outCurrency]
     * are skipped and accumulated in [ExchangedWalletBalance.exchangeErrors].
     *
     * @return empty map for zero balance
     */
    suspend fun calculate(outCurrency: AssetCode): ExchangedWalletBalance {
        TODO("Not implemented")
    }

    /**
     * Calculates the all-time balance of Ivy Wallet by summing
     * the balances of all included (not excluded) accounts.
     * The balance can be negative.
     *
     * @return empty map for zero balance
     */
    suspend fun calculate(): Map<AssetCode, NonZeroDouble> {
        TODO("Not implemented")
    }
}

data class ExchangedWalletBalance(
    val value: Option<Value>,
    val exchangeErrors: Set<AssetCode>
)