package kg.ivy.wallet.domain.action.account

import arrow.core.nonEmptyListOf
import kg.ivy.base.time.TimeProvider
import kg.ivy.data.model.Account
import kg.ivy.frp.action.FPAction
import kg.ivy.frp.then
import kg.ivy.legacy.domain.pure.transaction.AccountValueFunctions
import kg.ivy.wallet.domain.pure.data.ClosedTimeRange
import kg.ivy.wallet.domain.pure.transaction.foldTransactions
import java.math.BigDecimal
import javax.inject.Inject

class CalcAccBalanceAct @Inject constructor(
    private val accTrnsAct: AccTrnsAct,
    private val timeProvider: TimeProvider,
) : FPAction<CalcAccBalanceAct.Input, CalcAccBalanceAct.Output>() {

    override suspend fun Input.compose(): suspend () -> Output = suspend {
        AccTrnsAct.Input(
            accountId = account.id.value,
            range = range ?: ClosedTimeRange.allTimeIvy(timeProvider)
        )
    } then accTrnsAct then { accTrns ->
        foldTransactions(
            transactions = accTrns,
            arg = account.id.value,
            valueFunctions = nonEmptyListOf(AccountValueFunctions::balance)
        ).head
    } then { balance ->
        Output(
            account = account, balance = balance
        )
    }

    @Suppress("DataClassDefaultValues")
    data class Input(
        val account: Account,
        val range: ClosedTimeRange? = null
    )

    data class Output(
        val account: Account,
        val balance: BigDecimal,
    )
}
