package kg.ivy.legacy.data

import androidx.compose.runtime.Immutable
import kg.ivy.data.model.Category
import kg.ivy.legacy.datamodel.Account
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class AppBaseData(
    val baseCurrency: String,
    val accounts: ImmutableList<Account>,
    val categories: ImmutableList<Category>
)
