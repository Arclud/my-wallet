package kg.ivy.legacy.domain.deprecated.logic.loantrasactions

import androidx.compose.ui.graphics.toArgb
import kg.ivy.base.legacy.Transaction
import kg.ivy.base.model.LoanRecordType
import kg.ivy.base.model.TransactionType
import kg.ivy.base.time.TimeProvider
import kg.ivy.data.db.dao.read.AccountDao
import kg.ivy.data.db.dao.read.LoanDao
import kg.ivy.data.db.dao.read.LoanRecordDao
import kg.ivy.data.db.dao.read.SettingsDao
import kg.ivy.data.db.dao.read.TransactionDao
import kg.ivy.data.db.dao.write.WriteLoanDao
import kg.ivy.data.db.dao.write.WriteLoanRecordDao
import kg.ivy.data.model.Category
import kg.ivy.data.model.CategoryId
import kg.ivy.data.model.LoanType
import kg.ivy.data.model.TransactionId
import kg.ivy.data.model.primitive.ColorInt
import kg.ivy.data.model.primitive.IconAsset
import kg.ivy.data.model.primitive.NotBlankTrimmedString
import kg.ivy.data.repository.CategoryRepository
import kg.ivy.data.repository.TransactionRepository
import kg.ivy.data.repository.mapper.TransactionMapper
import kg.ivy.design.IVY_COLOR_PICKER_COLORS_FREE
import kg.ivy.legacy.IvyWalletCtx
import kg.ivy.legacy.datamodel.Account
import kg.ivy.legacy.datamodel.Loan
import kg.ivy.legacy.datamodel.LoanRecord
import kg.ivy.legacy.datamodel.temp.toDomain
import kg.ivy.legacy.datamodel.temp.toLegacyDomain
import kg.ivy.legacy.utils.computationThread
import kg.ivy.legacy.utils.ioThread
import kg.ivy.wallet.domain.deprecated.logic.currency.ExchangeRatesLogic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class LoanTransactionsCore @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val transactionDao: TransactionDao,
    private val ivyContext: IvyWalletCtx,
    private val loanRecordDao: LoanRecordDao,
    private val loanDao: LoanDao,
    private val settingsDao: SettingsDao,
    private val accountsDao: AccountDao,
    private val exchangeRatesLogic: ExchangeRatesLogic,
    private val transactionRepo: TransactionRepository,
    private val transactionMapper: TransactionMapper,
    private val writeLoanRecordDao: WriteLoanRecordDao,
    private val writeLoanDao: WriteLoanDao,
    private val timeProvider: TimeProvider,
) {
    private var baseCurrencyCode: String? = null

    companion object {
        const val DEFAULT_COLOR_INDEX = 4
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            baseCurrencyCode = baseCurrency()
        }
    }

    suspend fun deleteAssociatedTransactions(
        loanId: UUID? = null,
        loanRecordId: UUID? = null
    ) {
        if (loanId == null && loanRecordId == null) {
            return
        }

        ioThread {
            val transactions: List<Transaction?> =
                if (loanId != null) {
                    transactionDao.findAllByLoanId(loanId = loanId)
                        .map { it.toLegacyDomain() }
                } else {
                    listOf(transactionDao.findLoanRecordTransaction(loanRecordId!!)).map { it?.toLegacyDomain() }
                }

            transactions.forEach { trans ->
                deleteTransaction(trans)
            }
        }
    }

    fun findAccount(
        accounts: List<Account>,
        accountId: UUID?,
    ): Account? {
        return accountId?.let { uuid ->
            accounts.find { acc ->
                acc.id == uuid
            }
        }
    }

    suspend fun baseCurrency(): String =
        ioThread { baseCurrencyCode ?: settingsDao.findFirst().currency }

    suspend fun updateAssociatedTransaction(
        createTransaction: Boolean,
        loanRecordId: UUID? = null,
        loanId: UUID,
        amount: Double,
        loanType: LoanType,
        selectedAccountId: UUID?,
        title: String? = null,
        category: Category? = null,
        time: Instant? = null,
        isLoanRecord: Boolean = false,
        transaction: Transaction? = null,
        loanRecordType: LoanRecordType
    ) {
        if (isLoanRecord && loanRecordId == null) {
            return
        }

        if (createTransaction && transaction != null) {
            createMainTransaction(
                loanRecordId = loanRecordId,
                loanId = loanId,
                amount = amount,
                loanType = loanType,
                selectedAccountId = selectedAccountId,
                title = title ?: transaction.title,
                categoryId = category?.id?.value ?: transaction.categoryId,
                time = time ?: transaction.dateTime ?: timeProvider.utcNow(),
                isLoanRecord = isLoanRecord,
                transaction = transaction,
                loanRecordType = loanRecordType
            )
        } else if (createTransaction && transaction == null) {
            createMainTransaction(
                loanRecordId = loanRecordId,
                loanId = loanId,
                amount = amount,
                loanType = loanType,
                selectedAccountId = selectedAccountId,
                title = title,
                categoryId = category?.id?.value,
                time = time ?: timeProvider.utcNow(),
                isLoanRecord = isLoanRecord,
                transaction = transaction,
                loanRecordType = loanRecordType
            )
        } else {
            deleteTransaction(transaction = transaction)
        }
    }

    private suspend fun createMainTransaction(
        loanRecordId: UUID? = null,
        amount: Double,
        loanType: LoanType,
        loanId: UUID,
        selectedAccountId: UUID?,
        title: String? = null,
        categoryId: UUID? = null,
        time: Instant = timeProvider.utcNow(),
        isLoanRecord: Boolean = false,
        transaction: Transaction? = null,
        loanRecordType: LoanRecordType
    ) {
        if (selectedAccountId == null) {
            return
        }

        val transType = if (isLoanRecord && loanRecordType != LoanRecordType.INCREASE) {
            if (loanType == LoanType.BORROW) TransactionType.EXPENSE else TransactionType.INCOME
        } else if (loanType == LoanType.BORROW) TransactionType.INCOME else TransactionType.EXPENSE

        val transCategoryId: UUID? = getCategoryId(existingCategoryId = categoryId)

        val modifiedTransaction: Transaction = transaction?.copy(
            loanId = loanId,
            loanRecordId = if (isLoanRecord) loanRecordId else null,
            amount = amount.toBigDecimal(),
            type = transType,
            accountId = selectedAccountId,
            title = title,
            categoryId = transCategoryId,
            dateTime = time
        )
            ?: Transaction(
                accountId = selectedAccountId,
                type = transType,
                amount = amount.toBigDecimal(),
                dateTime = time,
                categoryId = transCategoryId,
                title = title,
                loanId = loanId,
                loanRecordId = if (isLoanRecord) loanRecordId else null
            )

        ioThread {
            modifiedTransaction.toDomain(transactionMapper)?.let {
                transactionRepo.save(it)
            }
        }
    }

    private suspend fun deleteTransaction(transaction: Transaction?) {
        ioThread {
            transaction?.let {
                transactionRepo.deleteById(TransactionId(it.id))
            }
        }
    }

    private suspend fun getCategoryId(existingCategoryId: UUID? = null): UUID? {
        if (existingCategoryId != null) {
            return existingCategoryId
        }

        val categoryList = ioThread {
            categoryRepository.findAll()
        }

        var addCategoryToDb = false

        val loanCategory = categoryList.find { category ->
            category.name.value.lowercase(Locale.ENGLISH).contains("loan")
        } ?: if (ivyContext.isPremium || categoryList.size < 12) {
            addCategoryToDb = true

            Category(
                name = NotBlankTrimmedString.unsafe("Loans"),
                color = ColorInt(IVY_COLOR_PICKER_COLORS_FREE[DEFAULT_COLOR_INDEX].toArgb()),
                icon = IconAsset.unsafe("loan"),
                id = CategoryId(UUID.randomUUID()),
                orderNum = 0.0,
            )
        } else {
            null
        }

        if (addCategoryToDb) {
            ioThread {
                loanCategory?.let {
                    categoryRepository.save(it)
                }
            }
        }

        return loanCategory?.id?.value
    }

    suspend fun computeConvertedAmount(
        oldLoanRecordAccountId: UUID?,
        oldLonRecordConvertedAmount: Double?,
        oldLoanRecordAmount: Double,
        newLoanRecordAccountID: UUID?,
        newLoanRecordAmount: Double,
        loanAccountId: UUID?,
        accounts: List<Account>,
        reCalculateLoanAmount: Boolean = false,
    ): Double? {
        return computationThread {
            val newLoanRecordCurrency =
                newLoanRecordAccountID.fetchAssociatedCurrencyCode(accountsList = accounts)

            val oldLoanRecordCurrency =
                oldLoanRecordAccountId.fetchAssociatedCurrencyCode(accountsList = accounts)

            val loanCurrency = loanAccountId.fetchAssociatedCurrencyCode(accountsList = accounts)

            val loanRecordCurrenciesChanged = oldLoanRecordCurrency != newLoanRecordCurrency

            val newConverted: Double? = when {
                newLoanRecordCurrency == loanCurrency -> {
                    null
                }

                reCalculateLoanAmount || loanRecordCurrenciesChanged ||
                        oldLonRecordConvertedAmount == null -> {
                    ioThread {
                        exchangeRatesLogic.convertAmount(
                            baseCurrency = baseCurrency(),
                            amount = newLoanRecordAmount,
                            fromCurrency = newLoanRecordCurrency,
                            toCurrency = loanCurrency
                        )
                    }
                }

                oldLoanRecordAmount != newLoanRecordAmount -> {
                    newLoanRecordAmount * (oldLonRecordConvertedAmount / oldLoanRecordAmount)
                }

                else -> {
                    oldLonRecordConvertedAmount
                }
            }
            newConverted
        }
    }

    private suspend fun UUID?.fetchAssociatedCurrencyCode(accountsList: List<Account>): String {
        return findAccount(accountsList, this)?.currency ?: baseCurrency()
    }

    suspend fun fetchAccounts() = ioThread {
        accountsDao.findAll()
    }

    suspend fun saveLoanRecords(loanRecords: List<LoanRecord>) = ioThread {
        writeLoanRecordDao.saveMany(loanRecords.map { it.toEntity() })
    }

    suspend fun saveLoanRecords(loanRecord: LoanRecord) = ioThread {
        writeLoanRecordDao.save(loanRecord.toEntity())
    }

    suspend fun saveLoan(loan: Loan) = ioThread {
        writeLoanDao.save(loan.toEntity())
    }

    suspend fun fetchLoanRecord(loanRecordId: UUID) = ioThread {
        loanRecordDao.findById(loanRecordId)
    }

    suspend fun fetchAllLoanRecords(loanId: UUID) = ioThread {
        loanRecordDao.findAllByLoanId(loanId)
    }

    suspend fun fetchLoan(loanId: UUID) = ioThread {
        loanDao.findById(loanId)
    }

    suspend fun fetchLoanRecordTransaction(loanRecordId: UUID?): Transaction? {
        return loanRecordId?.let {
            ioThread {
                transactionDao.findLoanRecordTransaction(it)?.toLegacyDomain()
            }
        }
    }
}
