package kg.ivy.accounts

sealed interface AccountsEvent {
    data class OnReorder(val reorderedList: List<kg.ivy.legacy.data.model.AccountData>) :
        AccountsEvent
    data class OnReorderModalVisible(val reorderVisible: Boolean) : AccountsEvent
}
