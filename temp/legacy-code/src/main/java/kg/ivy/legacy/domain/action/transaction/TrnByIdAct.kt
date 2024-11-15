package kg.ivy.wallet.domain.action.transaction

import kg.ivy.base.legacy.Transaction
import kg.ivy.data.model.TransactionId
import kg.ivy.data.repository.TransactionRepository
import kg.ivy.data.repository.mapper.TransactionMapper
import kg.ivy.frp.action.FPAction
import kg.ivy.frp.then
import kg.ivy.legacy.datamodel.temp.toLegacy
import java.util.UUID
import javax.inject.Inject

class TrnByIdAct @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val mapper: TransactionMapper
) : FPAction<UUID, Transaction?>() {
    override suspend fun UUID.compose(): suspend () -> Transaction? = suspend {
        this // transactionId
    } then {
        transactionRepo.findById(TransactionId(it))
    } then {
        it?.toLegacy(mapper)
    }
}
