package kg.ivy.wallet.domain.action.viewmodel.home

import kg.ivy.frp.action.FPAction
import kg.ivy.frp.then
import kg.ivy.legacy.utils.ivyMinTime
import kg.ivy.wallet.domain.pure.data.ClosedTimeRange
import kg.ivy.wallet.domain.pure.data.IncomeExpensePair
import kg.ivy.wallet.domain.pure.transaction.isOverdue
import java.time.Instant
import javax.inject.Inject

class OverdueAct @Inject constructor(
    private val dueTrnsInfoAct: DueTrnsInfoAct
) : FPAction<OverdueAct.Input, OverdueAct.Output>() {

    override suspend fun Input.compose(): suspend () -> Output = suspend {
        DueTrnsInfoAct.Input(
            range = ClosedTimeRange(
                from = ivyMinTime(),
                to = toRange
            ),
            baseCurrency = baseCurrency,
            dueFilter = ::isOverdue
        )
    } then dueTrnsInfoAct then {
        Output(
            overdue = it.dueIncomeExpense,
            overdueTrns = it.dueTrns
        )
    }

    data class Input(
        val toRange: Instant,
        val baseCurrency: String
    )

    data class Output(
        val overdue: IncomeExpensePair,
        val overdueTrns: List<kg.ivy.data.model.Transaction>
    )
}
