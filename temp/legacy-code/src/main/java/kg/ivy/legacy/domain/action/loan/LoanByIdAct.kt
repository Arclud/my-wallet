package kg.ivy.wallet.domain.action.loan

import kg.ivy.data.db.dao.read.LoanDao
import kg.ivy.frp.action.FPAction
import kg.ivy.legacy.datamodel.Loan
import kg.ivy.legacy.datamodel.temp.toLegacyDomain
import java.util.UUID
import javax.inject.Inject

class LoanByIdAct @Inject constructor(
    private val loanDao: LoanDao
) : FPAction<UUID, Loan?>() {
    override suspend fun UUID.compose(): suspend () -> Loan? = suspend {
        loanDao.findById(this)?.toLegacyDomain()
    }
}
