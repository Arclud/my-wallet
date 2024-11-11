package kg.ivy.wallet.domain.action.viewmodel.transaction

import kg.ivy.base.legacy.Transaction
import kg.ivy.data.repository.TransactionRepository
import kg.ivy.frp.action.FPAction
import kg.ivy.frp.then
import kg.ivy.legacy.datamodel.toEntity
import javax.inject.Inject

class SaveTrnLocallyAct @Inject constructor(
    private val transactionRepo: TransactionRepository,
) : FPAction<Transaction, Unit>() {
    override suspend fun Transaction.compose(): suspend () -> Unit = {
        this.copy(
            isSynced = false
        ).toEntity()
    } then {
        transactionRepo::save then {}
    }
}
