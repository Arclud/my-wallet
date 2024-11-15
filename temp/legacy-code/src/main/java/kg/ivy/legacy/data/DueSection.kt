package kg.ivy.legacy.data

import androidx.compose.runtime.Immutable
import kg.ivy.base.legacy.Transaction
import kg.ivy.wallet.domain.pure.data.IncomeExpensePair
import kotlinx.collections.immutable.ImmutableList

@Deprecated("Uses legacy Transaction")
@Immutable
data class LegacyDueSection(
    val trns: ImmutableList<Transaction>,
    val expanded: Boolean,
    val stats: IncomeExpensePair
)

@Deprecated("Legacy data model")
@Immutable
data class DueSection(
    val trns: ImmutableList<kg.ivy.data.model.Transaction>,
    val expanded: Boolean,
    val stats: IncomeExpensePair
)