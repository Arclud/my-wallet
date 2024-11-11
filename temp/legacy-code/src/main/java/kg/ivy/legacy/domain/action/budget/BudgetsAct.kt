package kg.ivy.wallet.domain.action.budget

import kg.ivy.data.db.dao.read.BudgetDao
import kg.ivy.frp.action.FPAction
import kg.ivy.frp.action.thenMap
import kg.ivy.frp.then
import kg.ivy.legacy.datamodel.Budget
import kg.ivy.legacy.datamodel.temp.toLegacyDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class BudgetsAct @Inject constructor(
    private val budgetDao: BudgetDao
) : FPAction<Unit, ImmutableList<Budget>>() {
    override suspend fun Unit.compose(): suspend () -> ImmutableList<Budget> = suspend {
        budgetDao.findAll()
    } thenMap { it.toLegacyDomain() } then { it.toImmutableList() }
}
