package kg.ivy.wallet.domain.data

import androidx.compose.runtime.Immutable
import kg.ivy.base.legacy.TransactionHistoryItem
import java.time.LocalDate

@Immutable
data class TransactionHistoryDateDivider(
    val date: LocalDate,
    val income: Double,
    val expenses: Double
) : TransactionHistoryItem
