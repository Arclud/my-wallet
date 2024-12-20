package kg.ivy.piechart

import androidx.compose.runtime.Immutable
import kg.ivy.base.legacy.Transaction
import kg.ivy.base.model.TransactionType
import kg.ivy.legacy.data.model.TimePeriod
import kg.ivy.wallet.ui.theme.modal.ChoosePeriodModalData
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

@Immutable
data class PieChartStatisticState(
    val transactionType: TransactionType,
    val period: TimePeriod,
    val baseCurrency: String,
    val totalAmount: Double,
    val categoryAmounts: ImmutableList<CategoryAmount>,
    val selectedCategory: SelectedCategory?,
    val accountIdFilterList: ImmutableList<UUID>,
    val showCloseButtonOnly: Boolean,
    val filterExcluded: Boolean,
    val transactions: ImmutableList<Transaction>,
    val choosePeriodModal: ChoosePeriodModalData?
)
