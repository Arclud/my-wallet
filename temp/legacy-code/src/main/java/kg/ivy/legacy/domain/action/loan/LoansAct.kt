package kg.ivy.wallet.domain.action.loan

import kg.ivy.data.db.dao.read.LoanDao
import kg.ivy.frp.action.FPAction
import kg.ivy.frp.action.thenMap
import kg.ivy.frp.then
import kg.ivy.legacy.datamodel.Loan
import kg.ivy.legacy.datamodel.temp.toLegacyDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class LoansAct @Inject constructor(
    private val loanDao: LoanDao
) : FPAction<Unit, ImmutableList<Loan>>() {
    override suspend fun Unit.compose(): suspend () -> ImmutableList<Loan> = suspend {
        loanDao.findAll()
    } thenMap { it.toLegacyDomain() } then { it.toImmutableList() }
}
