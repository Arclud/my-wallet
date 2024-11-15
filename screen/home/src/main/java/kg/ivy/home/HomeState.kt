package kg.ivy.home

import androidx.compose.runtime.Immutable
import kg.ivy.base.legacy.Theme
import kg.ivy.base.legacy.TransactionHistoryItem
import kg.ivy.home.customerjourney.CustomerJourneyCardModel
import kg.ivy.legacy.data.AppBaseData
import kg.ivy.legacy.data.BufferInfo
import kg.ivy.legacy.data.LegacyDueSection
import kg.ivy.legacy.data.model.TimePeriod
import kg.ivy.wallet.domain.pure.data.IncomeExpensePair
import kotlinx.collections.immutable.ImmutableList
import java.math.BigDecimal

@Immutable
data class HomeState(
    val theme: Theme,
    val name: String,

    val period: TimePeriod,
    val baseData: AppBaseData,

    val history: ImmutableList<TransactionHistoryItem>,
    val stats: IncomeExpensePair,

    val balance: BigDecimal,

    val buffer: BufferInfo,

    val upcoming: LegacyDueSection,
    val overdue: LegacyDueSection,

    val customerJourneyCards: ImmutableList<CustomerJourneyCardModel>,
    val hideBalance: Boolean,
    val hideIncome: Boolean,
    val expanded: Boolean,
    val shouldShowAccountSpecificColorInTransactions: Boolean
)
