package kg.ivy.wallet.domain.deprecated.logic

import arrow.core.getOrElse
import kg.ivy.base.legacy.SharedPrefs
import kg.ivy.base.legacy.Transaction
import kg.ivy.base.model.TransactionType
import kg.ivy.base.time.TimeProvider
import kg.ivy.data.model.AccountId
import kg.ivy.data.model.Expense
import kg.ivy.data.model.Income
import kg.ivy.data.repository.CurrencyRepository
import kg.ivy.data.repository.TransactionRepository
import kg.ivy.data.repository.mapper.TransactionMapper
import kg.ivy.data.temp.migration.getValue
import kg.ivy.legacy.data.model.filterOverdue
import kg.ivy.legacy.data.model.filterUpcoming
import kg.ivy.legacy.datamodel.Account
import kg.ivy.legacy.datamodel.temp.toDomain
import kg.ivy.wallet.domain.action.viewmodel.account.AccountDataAct
import kg.ivy.wallet.domain.pure.data.ClosedTimeRange
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.absoluteValue

@Deprecated("Migrate to FP Style")
class WalletAccountLogic @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val transactionMapper: TransactionMapper,
    private val accountDataAct: AccountDataAct,
    private val sharedPrefs: SharedPrefs,
    private val currencyRepository: CurrencyRepository,
    private val timeProvider: TimeProvider
) {

    suspend fun adjustBalance(
        account: Account,
        actualBalance: Double? = null,
        newBalance: Double,

        adjustTransactionTitle: String = "Adjust balance",

        isFiat: Boolean? = null,
        trnIsSyncedFlag: Boolean = false, // TODO: Remove this once Bank Integration trn sync is properly implemented
    ) {
        val ab = actualBalance ?: calculateAccountBalance(account)
        val diff = ab - newBalance

        val finalDiff = if (isFiat == true && abs(diff) < 0.009) 0.0 else diff
        when {
            finalDiff < 0 -> {
                // add income
                Transaction(
                    type = TransactionType.INCOME,
                    title = adjustTransactionTitle,
                    amount = diff.absoluteValue.toBigDecimal(),
                    toAmount = diff.absoluteValue.toBigDecimal(),
                    dateTime = timeProvider.utcNow(),
                    accountId = account.id,
                    isSynced = trnIsSyncedFlag
                ).toDomain(transactionMapper)?.let {
                    transactionRepository.save(it)
                }
            }

            finalDiff > 0 -> {
                // add expense
                Transaction(
                    type = TransactionType.EXPENSE,
                    title = adjustTransactionTitle,
                    amount = diff.absoluteValue.toBigDecimal(),
                    toAmount = diff.absoluteValue.toBigDecimal(),
                    dateTime = timeProvider.utcNow(),
                    accountId = account.id,
                    isSynced = trnIsSyncedFlag
                ).toDomain(transactionMapper)?.let {
                    transactionRepository.save(it)
                }
            }
        }
    }

    suspend fun calculateAccountBalance(
        account: Account
    ): Double {
        val accountList = account.toDomainAccount(currencyRepository)
            .map { a -> listOf(a) }
            .getOrElse { emptyList() }

        val includeTransfersInCalc =
            sharedPrefs.getBoolean(SharedPrefs.TRANSFERS_AS_INCOME_EXPENSE, false)

        val accountsDataList = accountDataAct(
            AccountDataAct.Input(
                accounts = accountList.toImmutableList(),
                range = ClosedTimeRange.allTimeIvy(timeProvider),
                baseCurrency = currencyRepository.getBaseCurrency().code,
                includeTransfersInCalc = includeTransfersInCalc
            )
        )

        return accountsDataList.firstOrNull()?.balance ?: 0.0
    }

    suspend fun calculateUpcomingIncome(
        account: Account,
        range: kg.ivy.legacy.data.model.FromToTimeRange
    ): Double =
        upcoming(account, range = range)
            .filterIsInstance<Income>()
            .sumOf { it.getValue().toDouble() }

    suspend fun calculateUpcomingExpenses(
        account: Account,
        range: kg.ivy.legacy.data.model.FromToTimeRange
    ): Double =
        upcoming(account = account, range = range)
            .filterIsInstance<Expense>()
            .sumOf { it.getValue().toDouble() }

    suspend fun calculateOverdueIncome(
        account: Account,
        range: kg.ivy.legacy.data.model.FromToTimeRange
    ): Double =
        overdue(account, range = range)
            .filterIsInstance<Income>()
            .sumOf { it.getValue().toDouble() }

    suspend fun calculateOverdueExpenses(
        account: Account,
        range: kg.ivy.legacy.data.model.FromToTimeRange
    ): Double =
        overdue(account, range = range)
            .filterIsInstance<Expense>()
            .sumOf { it.getValue().toDouble() }

    suspend fun upcoming(
        account: Account,
        range: kg.ivy.legacy.data.model.FromToTimeRange
    ): List<kg.ivy.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByAccount(
            accountId = AccountId(account.id),
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        ).filterUpcoming()
    }

    suspend fun overdue(
        account: Account,
        range: kg.ivy.legacy.data.model.FromToTimeRange
    ): List<kg.ivy.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByAccount(
            accountId = AccountId(account.id),
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        ).filterOverdue()
    }
}
