package kg.ivy.planned.edit

import kg.ivy.base.model.TransactionType
import kg.ivy.data.model.Category
import kg.ivy.data.model.IntervalType
import kg.ivy.legacy.datamodel.Account
import kg.ivy.wallet.domain.deprecated.logic.model.CreateAccountData
import kg.ivy.wallet.domain.deprecated.logic.model.CreateCategoryData
import kg.ivy.wallet.ui.theme.modal.RecurringRuleModalData
import kg.ivy.wallet.ui.theme.modal.edit.AccountModalData
import kg.ivy.wallet.ui.theme.modal.edit.CategoryModalData
import java.time.LocalDateTime

sealed interface EditPlannedScreenEvent {
    data class OnRuleChanged(
        val startDate: LocalDateTime,
        val oneTime: Boolean,
        val intervalN: Int?,
        val intervalType: IntervalType?
    ) : EditPlannedScreenEvent

    data class OnAmountChanged(val newAmount: Double) : EditPlannedScreenEvent
    data class OnTitleChanged(val newTitle: String?) : EditPlannedScreenEvent
    data class OnDescriptionChanged(val newDescription: String?) : EditPlannedScreenEvent
    data class OnCategoryChanged(val newCategory: Category?) : EditPlannedScreenEvent
    data class OnAccountChanged(val newAccount: Account) : EditPlannedScreenEvent
    data class OnSetTransactionType(val newTransactionType: TransactionType) :
        EditPlannedScreenEvent

    data class OnSave(val closeScreen: Boolean = true) : EditPlannedScreenEvent
    data object OnDelete : EditPlannedScreenEvent
    data class OnEditCategory(val updatedCategory: Category) : EditPlannedScreenEvent
    data class OnCreateCategory(val data: CreateCategoryData) : EditPlannedScreenEvent
    data class OnCreateAccount(val data: CreateAccountData) : EditPlannedScreenEvent
    data class OnCategoryModalVisible(val visible: Boolean) : EditPlannedScreenEvent
    data class OnDescriptionModalVisible(val visible: Boolean) : EditPlannedScreenEvent
    data class OnDeleteTransactionModalVisible(val visible: Boolean) : EditPlannedScreenEvent
    data class OnAmountModalVisible(val visible: Boolean) : EditPlannedScreenEvent
    data class OnTransactionTypeModalVisible(val visible: Boolean) : EditPlannedScreenEvent
    data class OnCategoryModalDataChanged(val categoryModalData: CategoryModalData?) :
        EditPlannedScreenEvent

    data class OnRecurringRuleModalDataChanged(val recurringRuleModalData: RecurringRuleModalData?) :
        EditPlannedScreenEvent

    data class OnAccountModalDataChanged(val accountModalData: AccountModalData?) :
        EditPlannedScreenEvent
}
