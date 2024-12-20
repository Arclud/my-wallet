package kg.ivy.legacy.ui.component.transaction

import androidx.compose.runtime.Composable
import kg.ivy.data.model.Category
import kg.ivy.legacy.datamodel.Account
import kg.ivy.legacy.ivyWalletCtx
import java.util.UUID

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun category(
    categoryId: UUID?,
    categories: List<Category>
): Category? {
    val targetId = categoryId ?: return null
    return ivyWalletCtx().categoryMap[targetId] ?: categories.find { it.id.value == targetId }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun account(
    accountId: UUID?,
    accounts: List<Account>
): Account? {
    val targetId = accountId ?: return null
    return ivyWalletCtx().accountMap[targetId] ?: accounts.find { it.id == targetId }
}
