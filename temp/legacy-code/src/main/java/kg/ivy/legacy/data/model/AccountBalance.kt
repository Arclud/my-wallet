package kg.ivy.legacy.data.model

import androidx.compose.runtime.Immutable
import kg.ivy.legacy.datamodel.Account

@Immutable
data class AccountBalance(
    val account: Account,
    val balance: Double
)
