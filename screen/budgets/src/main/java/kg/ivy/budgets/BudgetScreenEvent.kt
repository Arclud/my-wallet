package kg.ivy.budgets

import kg.ivy.budgets.model.DisplayBudget
import kg.ivy.legacy.datamodel.Budget
import kg.ivy.wallet.domain.deprecated.logic.model.CreateBudgetData

sealed interface BudgetScreenEvent {
    data class OnReorder(val newOrder: List<DisplayBudget>) : BudgetScreenEvent
    data class OnCreateBudget(val budgetData: CreateBudgetData) : BudgetScreenEvent
    data class OnEditBudget(val budget: Budget) : BudgetScreenEvent
    data class OnDeleteBudget(val budget: Budget) : BudgetScreenEvent
    data class OnReorderModalVisible(val visible: Boolean) : BudgetScreenEvent
    data class OnBudgetModalData(val budgetModalData: BudgetModalData?) : BudgetScreenEvent
}
