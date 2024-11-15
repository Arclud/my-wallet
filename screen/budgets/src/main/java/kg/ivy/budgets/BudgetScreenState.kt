package kg.ivy.budgets

import kg.ivy.budgets.model.DisplayBudget
import kg.ivy.data.model.Category
import kg.ivy.legacy.data.model.FromToTimeRange
import kg.ivy.legacy.datamodel.Account
import kotlinx.collections.immutable.ImmutableList
import javax.annotation.concurrent.Immutable

@Immutable
data class BudgetScreenState(
    val baseCurrency: String,
    val budgets: ImmutableList<DisplayBudget>,
    val categories: ImmutableList<Category>,
    val accounts: ImmutableList<Account>,
    val categoryBudgetsTotal: Double,
    val appBudgetMax: Double,
    val totalRemainingBudgetText: String?,
    val timeRange: FromToTimeRange?,
    val reorderModalVisible: Boolean,
    val budgetModalData: BudgetModalData?
)
