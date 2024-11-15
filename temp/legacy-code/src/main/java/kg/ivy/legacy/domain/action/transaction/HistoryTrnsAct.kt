package kg.ivy.wallet.domain.action.transaction

import kg.ivy.data.model.Transaction
import kg.ivy.data.repository.TransactionRepository
import kg.ivy.frp.action.FPAction
import kg.ivy.wallet.domain.pure.data.ClosedTimeRange
import javax.inject.Inject

class HistoryTrnsAct @Inject constructor(
    private val transactionRepository: TransactionRepository
) : FPAction<ClosedTimeRange, List<Transaction>>() {

    override suspend fun ClosedTimeRange.compose(): suspend () -> List<Transaction> = suspend {
        io {
            transactionRepository.findAllBetween(
                startDate = from,
                endDate = to
            )
        }
    }
}
