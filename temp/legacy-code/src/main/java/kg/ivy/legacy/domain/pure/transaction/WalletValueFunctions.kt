package kg.ivy.wallet.domain.pure.transaction

import kg.ivy.base.model.TransactionType
import kg.ivy.data.model.Expense
import kg.ivy.data.model.Income
import kg.ivy.data.model.Transaction
import kg.ivy.data.model.Transfer
import kg.ivy.data.temp.migration.getAccountId
import kg.ivy.frp.SideEffect
import kg.ivy.legacy.datamodel.Account
import kg.ivy.wallet.domain.pure.exchange.ExchangeEffect
import kg.ivy.wallet.domain.pure.exchange.LegacyExchangeTrns
import kg.ivy.wallet.domain.pure.exchange.exchangeInBaseCurrency
import java.math.BigDecimal

object WalletValueFunctions {
    data class Argument(
        val accounts: List<Account>,
        val baseCurrency: String,

        @SideEffect
        val exchange: ExchangeEffect
    )

    suspend fun income(
        transaction: Transaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        when (this) {
            is Income -> exchangeInBaseCurrency(
                transaction = this,
                accounts = arg.accounts,
                baseCurrency = arg.baseCurrency,
                exchange = arg.exchange
            )

            else -> BigDecimal.ZERO
        }
    }

    suspend fun transferIncome(
        transaction: Transaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        val condition = arg.accounts.any { it.id == (this as? Transfer)?.toAccount?.value }
        if (!condition) {
            return BigDecimal.ZERO
        }

        when (this) {
            is Transfer -> exchangeInBaseCurrency(
                transaction = this,
                accounts = arg.accounts,
                baseCurrency = arg.baseCurrency,
                exchange = arg.exchange
            )

            else -> BigDecimal.ZERO
        }
    }

    suspend fun expense(
        transaction: Transaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        when (this) {
            is Expense -> exchangeInBaseCurrency(
                transaction = this,
                accounts = arg.accounts,
                baseCurrency = arg.baseCurrency,
                exchange = arg.exchange
            )

            else -> BigDecimal.ZERO
        }
    }

    suspend fun transferExpenses(
        transaction: Transaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        val condition = arg.accounts.any { it.id == this.getAccountId() }
        if (!condition) {
            return BigDecimal.ZERO
        }
        when (this) {
            is Transfer -> exchangeInBaseCurrency(
                transaction = this,
                accounts = arg.accounts,
                baseCurrency = arg.baseCurrency,
                exchange = arg.exchange
            )

            else -> BigDecimal.ZERO
        }
    }
}

@Deprecated("Uses legacy Transaction")
object WalletValueFunctionsLegacy {
    data class Argument(
        val accounts: List<Account>,
        val baseCurrency: String,

        @SideEffect
        val exchange: ExchangeEffect
    )

    suspend fun income(
        transaction: kg.ivy.base.legacy.Transaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        when (type) {
            TransactionType.INCOME -> LegacyExchangeTrns.exchangeInBaseCurrency(
                transaction = this,
                accounts = arg.accounts,
                baseCurrency = arg.baseCurrency,
                exchange = arg.exchange
            )

            else -> BigDecimal.ZERO
        }
    }

    suspend fun transferIncome(
        transaction: kg.ivy.base.legacy.Transaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        val condition = arg.accounts.any { it.id == this.toAccountId }
        when {
            type == TransactionType.TRANSFER && condition ->
                LegacyExchangeTrns.exchangeInBaseCurrency(
                    transaction = this.copy(
                        amount = this.toAmount,
                        accountId = this.toAccountId ?: this.accountId
                    ), // Do not remove copy()
                    accounts = arg.accounts,
                    baseCurrency = arg.baseCurrency,
                    exchange = arg.exchange
                )

            else -> BigDecimal.ZERO
        }
    }

    suspend fun expense(
        transaction: kg.ivy.base.legacy.Transaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        when (type) {
            TransactionType.EXPENSE -> LegacyExchangeTrns.exchangeInBaseCurrency(
                transaction = this,
                accounts = arg.accounts,
                baseCurrency = arg.baseCurrency,
                exchange = arg.exchange
            )

            else -> BigDecimal.ZERO
        }
    }

    suspend fun transferExpenses(
        transaction: kg.ivy.base.legacy.Transaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        val condition = arg.accounts.any { it.id == this.accountId }
        when {
            type == TransactionType.TRANSFER && condition -> LegacyExchangeTrns.exchangeInBaseCurrency(
                transaction = this,
                accounts = arg.accounts,
                baseCurrency = arg.baseCurrency,
                exchange = arg.exchange
            )

            else -> BigDecimal.ZERO
        }
    }
}
