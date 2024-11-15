package kg.ivy.wallet.domain.action.viewmodel.home

import kg.ivy.frp.action.FPAction
import kg.ivy.legacy.IvyWalletCtx
import kg.ivy.legacy.datamodel.Account
import javax.inject.Inject

class UpdateAccCacheAct @Inject constructor(
    private val ivyWalletCtx: IvyWalletCtx
) : FPAction<List<Account>, List<Account>>() {
    override suspend fun List<Account>.compose(): suspend () -> List<Account> = suspend {
        val accounts = this

        ivyWalletCtx.accountMap.clear()
        ivyWalletCtx.accountMap.putAll(accounts.map { it.id to it })

        accounts
    }
}
