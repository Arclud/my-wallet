package kg.ivy.wallet.domain.action.account

import kg.ivy.data.db.dao.read.AccountDao
import kg.ivy.frp.action.FPAction
import kg.ivy.frp.then
import kg.ivy.legacy.datamodel.Account
import kg.ivy.legacy.datamodel.temp.toLegacyDomain
import java.util.UUID
import javax.inject.Inject

class AccountByIdAct @Inject constructor(
    private val accountDao: AccountDao
) : FPAction<UUID, Account?>() {
    @Deprecated("Legacy code. Don't use it, please.")
    override suspend fun UUID.compose(): suspend () -> Account? = suspend {
        this // accountId
    } then accountDao::findById then {
        it?.toLegacyDomain()
    }
}
