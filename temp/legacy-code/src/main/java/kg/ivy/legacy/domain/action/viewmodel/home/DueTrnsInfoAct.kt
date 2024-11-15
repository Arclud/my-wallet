package kg.ivy.wallet.domain.action.viewmodel.home

import kg.ivy.base.time.TimeProvider
import kg.ivy.data.model.Transaction
import kg.ivy.frp.action.FPAction
import kg.ivy.frp.lambda
import kg.ivy.frp.then
import kg.ivy.wallet.domain.action.account.AccountByIdAct
import kg.ivy.wallet.domain.action.exchange.ExchangeAct
import kg.ivy.wallet.domain.action.exchange.actInput
import kg.ivy.wallet.domain.action.transaction.DueTrnsAct
import kg.ivy.wallet.domain.pure.data.ClosedTimeRange
import kg.ivy.wallet.domain.pure.data.IncomeExpensePair
import kg.ivy.wallet.domain.pure.exchange.ExchangeTrnArgument
import kg.ivy.wallet.domain.pure.exchange.exchangeInBaseCurrency
import kg.ivy.wallet.domain.pure.transaction.expenses
import kg.ivy.wallet.domain.pure.transaction.incomes
import kg.ivy.wallet.domain.pure.transaction.sumTrns
import java.time.LocalDate
import javax.inject.Inject

class DueTrnsInfoAct @Inject constructor(
    private val dueTrnsAct: DueTrnsAct,
    private val accountByIdAct: AccountByIdAct,
    private val exchangeAct: ExchangeAct,
    private val timeProvider: TimeProvider
) : FPAction<DueTrnsInfoAct.Input, DueTrnsInfoAct.Output>() {

    override suspend fun Input.compose(): suspend () -> Output =
        suspend {
            range
        } then dueTrnsAct then { trns ->
            val dateNow = timeProvider.localDateNow()
            trns.filter {
                this.dueFilter(it, dateNow)
            }
        } then { dueTrns ->
            // We have due transactions in different currencies
            val exchangeArg = ExchangeTrnArgument(
                baseCurrency = baseCurrency,
                exchange = ::actInput then exchangeAct,
                getAccount = accountByIdAct.lambda()
            )

            Output(
                dueIncomeExpense = IncomeExpensePair(
                    income = sumTrns(
                        incomes(dueTrns),
                        ::exchangeInBaseCurrency,
                        exchangeArg
                    ),
                    expense = sumTrns(
                        expenses(dueTrns),
                        ::exchangeInBaseCurrency,
                        exchangeArg
                    )
                ),
                dueTrns = dueTrns
            )
        }

    data class Input(
        val range: ClosedTimeRange,
        val baseCurrency: String,
        val dueFilter: (Transaction, LocalDate) -> Boolean
    )

    data class Output(
        val dueIncomeExpense: IncomeExpensePair,
        val dueTrns: List<Transaction>
    )
}
