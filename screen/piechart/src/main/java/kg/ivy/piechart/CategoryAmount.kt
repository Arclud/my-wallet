package kg.ivy.piechart

import androidx.compose.runtime.Immutable
import kg.ivy.base.legacy.Transaction
import kg.ivy.data.model.Category

@Immutable
data class CategoryAmount(
    val category: Category?,
    val amount: Double,
    val associatedTransactions: List<Transaction> = emptyList(),
    val isCategoryUnspecified: Boolean = false
)
