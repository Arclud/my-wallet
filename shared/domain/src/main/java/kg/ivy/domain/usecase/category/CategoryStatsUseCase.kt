package kg.ivy.domain.usecase.category

import arrow.core.Option
import kg.ivy.base.threading.DispatchersProvider
import kg.ivy.data.model.CategoryId
import kg.ivy.data.model.PositiveValue
import kg.ivy.data.model.Transaction
import kg.ivy.data.model.primitive.AssetCode
import kg.ivy.domain.model.StatSummary
import kg.ivy.domain.model.TimeRange
import kg.ivy.domain.usecase.exchange.ExchangeUseCase
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Suppress("UnusedPrivateProperty", "UnusedParameter")
class CategoryStatsUseCase @Inject constructor(
    private val dispatchers: DispatchersProvider,
    private val exchangeUseCase: ExchangeUseCase,
) {
    suspend fun calculate(
        category: CategoryId,
        range: TimeRange,
        outCurrency: AssetCode,
    ): ExchangedCategoryStats {
        TODO("Not implemented")
    }

    suspend fun calculate(
        category: CategoryId,
        range: TimeRange,
    ): CategoryStats {
        TODO("Not implemented")
    }

    suspend fun calculate(
        category: CategoryId,
        transactions: List<Transaction>,
        outCurrency: AssetCode,
    ): ExchangedCategoryStats {
        TODO("Not implemented")
    }

    suspend fun calculate(
        category: CategoryId,
        transactions: List<Transaction>
    ): CategoryStats = withContext(dispatchers.default) {
        TODO("Not implemented")
    }
}

data class CategoryStats(
    val income: StatSummary,
    val expense: StatSummary,
) {
    companion object {
        val Zero = CategoryStats(
            income = StatSummary.Zero,
            expense = StatSummary.Zero,
        )
    }
}

data class ExchangedCategoryStats(
    val income: Option<PositiveValue>,
    val expense: Option<PositiveValue>,
    val exchangeErrors: Set<AssetCode>
)