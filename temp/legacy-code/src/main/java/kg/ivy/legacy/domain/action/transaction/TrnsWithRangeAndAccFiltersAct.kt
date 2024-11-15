package kg.ivy.wallet.domain.action.transaction

import kg.ivy.base.legacy.Transaction
import kg.ivy.data.db.dao.read.TransactionDao
import kg.ivy.frp.action.FPAction
import kg.ivy.frp.action.thenFilter
import kg.ivy.legacy.datamodel.temp.toLegacyDomain
import java.util.UUID
import javax.inject.Inject

class TrnsWithRangeAndAccFiltersAct @Inject constructor(
    private val transactionDao: TransactionDao
) : FPAction<TrnsWithRangeAndAccFiltersAct.Input, List<Transaction>>() {

    override suspend fun Input.compose(): suspend () -> List<Transaction> = suspend {
        transactionDao.findAllBetween(range.from(), range.to())
            .map { it.toLegacyDomain() }
    } thenFilter {
        accountIdFilterSet.contains(it.accountId) || accountIdFilterSet.contains(it.toAccountId)
    }

    data class Input(
        val range: kg.ivy.legacy.data.model.FromToTimeRange,
        val accountIdFilterSet: Set<UUID>
    )
}
