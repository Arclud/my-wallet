package kg.ivy.wallet.domain.action.wallet

import arrow.core.nonEmptyListOf
import arrow.core.toOption
import kg.ivy.frp.action.FPAction
import kg.ivy.frp.action.thenMap
import kg.ivy.frp.then
import kg.ivy.legacy.datamodel.Account
import kg.ivy.wallet.domain.action.account.AccTrnsAct
import kg.ivy.wallet.domain.action.exchange.ExchangeAct
import kg.ivy.wallet.domain.pure.account.filterExcluded
import kg.ivy.wallet.domain.pure.data.ClosedTimeRange
import kg.ivy.wallet.domain.pure.data.IncomeExpensePair
import kg.ivy.wallet.domain.pure.exchange.ExchangeData
import kg.ivy.legacy.domain.pure.transaction.AccountValueFunctions
import kg.ivy.wallet.domain.pure.transaction.foldTransactions
import kg.ivy.wallet.domain.pure.util.orZero
import timber.log.Timber
import javax.inject.Inject

class CalcIncomeExpenseAct @Inject constructor(
    private val accTrnsAct: AccTrnsAct,
    private val exchangeAct: ExchangeAct
) : FPAction<CalcIncomeExpenseAct.Input, IncomeExpensePair>() {

    override suspend fun Input.compose(): suspend () -> IncomeExpensePair = suspend {
        filterExcluded(accounts)
    } thenMap { acc ->
        Pair(
            acc,
            accTrnsAct(
                AccTrnsAct.Input(
                    accountId = acc.id,
                    range = range
                )
            )
        )
    } thenMap { (acc, trns) ->
        Timber.i("acc: $acc, trns = ${trns.size}")
        Pair(
            acc,
            foldTransactions(
                transactions = trns,
                valueFunctions = nonEmptyListOf(
                    AccountValueFunctions::income,
                    AccountValueFunctions::expense
                ),
                arg = acc.id
            )
        )
    } thenMap { (acc, stats) ->
        Timber.i("acc_stats: $acc - $stats")
        stats.map {
            exchangeAct(
                ExchangeAct.Input(
                    data = ExchangeData(
                        baseCurrency = baseCurrency,
                        fromCurrency = (acc.currency ?: baseCurrency).toOption()
                    ),
                    amount = it
                ),
            ).orZero()
        }
    } then { statsList ->
        IncomeExpensePair(
            income = statsList.sumOf { it[0] },
            expense = statsList.sumOf { it[1] }
        )
    }

    data class Input(
        val baseCurrency: String,
        val accounts: List<Account>,
        val range: ClosedTimeRange,
    )
}
