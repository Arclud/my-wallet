package kg.ivy.wallet.domain.action.viewmodel.home

import kg.ivy.data.model.Category
import kg.ivy.frp.action.FPAction
import kg.ivy.legacy.IvyWalletCtx
import javax.inject.Inject

class UpdateCategoriesCacheAct @Inject constructor(
    private val ivyWalletCtx: IvyWalletCtx
) : FPAction<List<Category>, List<Category>>() {
    override suspend fun List<Category>.compose(): suspend () -> List<Category> = suspend {
        val categories = this

        ivyWalletCtx.categoryMap.clear()
        ivyWalletCtx.categoryMap.putAll(categories.map { it.id.value to it })

        categories
    }
}
