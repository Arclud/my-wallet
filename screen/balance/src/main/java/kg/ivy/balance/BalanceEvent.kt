package kg.ivy.balance

import kg.ivy.legacy.data.model.TimePeriod

sealed interface BalanceEvent {
    data class OnSetPeriod(val timePeriod: TimePeriod) : BalanceEvent
    data object OnPreviousMonth : BalanceEvent
    data object OnNextMonth : BalanceEvent
}
