package kg.ivy.wallet.domain.action.transaction

import kg.ivy.data.model.Transaction
import kg.ivy.data.repository.TransactionRepository
import kg.ivy.frp.action.FPAction
import javax.inject.Inject

class AllTrnsAct @Inject constructor(
    private val transactionRepository: TransactionRepository
) : FPAction<Unit, List<Transaction>>() {
    override suspend fun Unit.compose(): suspend () -> List<Transaction> = suspend {
        transactionRepository.findAll()
    }
}
