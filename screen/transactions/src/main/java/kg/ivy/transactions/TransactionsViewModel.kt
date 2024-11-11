package kg.ivy.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import arrow.core.toOption
import kg.ivy.base.legacy.SharedPrefs
import kg.ivy.base.legacy.Transaction
import kg.ivy.base.legacy.TransactionHistoryItem
import kg.ivy.base.legacy.stringRes
import kg.ivy.base.model.TransactionType
import kg.ivy.base.time.TimeConverter
import kg.ivy.base.time.TimeProvider
import kg.ivy.data.db.dao.read.AccountDao
import kg.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import kg.ivy.data.model.AccountId
import kg.ivy.data.model.Category
import kg.ivy.data.model.CategoryId
import kg.ivy.data.model.primitive.ColorInt
import kg.ivy.data.model.primitive.IconAsset
import kg.ivy.data.model.primitive.NotBlankTrimmedString
import kg.ivy.data.repository.AccountRepository
import kg.ivy.data.repository.CategoryRepository
import kg.ivy.data.repository.TagRepository
import kg.ivy.data.repository.TransactionRepository
import kg.ivy.data.repository.mapper.TransactionMapper
import kg.ivy.design.l0_system.RedLight
import kg.ivy.domain.features.Features
import kg.ivy.frp.then
import kg.ivy.legacy.IvyWalletCtx
import kg.ivy.legacy.data.model.TimePeriod
import kg.ivy.legacy.data.model.toCloseTimeRange
import kg.ivy.legacy.datamodel.temp.toImmutableLegacyTags
import kg.ivy.legacy.datamodel.temp.toLegacyDomain
import kg.ivy.legacy.domain.deprecated.logic.AccountCreator
import kg.ivy.legacy.utils.computationThread
import kg.ivy.legacy.utils.dateNowUTC
import kg.ivy.legacy.utils.ioThread
import kg.ivy.legacy.utils.isNotNullOrBlank
import kg.ivy.legacy.utils.selectEndTextFieldValue
import kg.ivy.navigation.Navigation
import kg.ivy.navigation.TransactionsScreen
import kg.ivy.ui.ComposeViewModel
import kg.ivy.ui.R
import kg.ivy.wallet.domain.action.account.AccTrnsAct
import kg.ivy.wallet.domain.action.account.AccountsAct
import kg.ivy.wallet.domain.action.account.CalcAccBalanceAct
import kg.ivy.wallet.domain.action.account.CalcAccIncomeExpenseAct
import kg.ivy.wallet.domain.action.exchange.ExchangeAct
import kg.ivy.wallet.domain.action.settings.BaseCurrencyAct
import kg.ivy.wallet.domain.action.transaction.LegacyCalcTrnsIncomeExpenseAct
import kg.ivy.wallet.domain.action.transaction.LegacyTrnsWithDateDivsAct
import kg.ivy.wallet.domain.deprecated.logic.CategoryCreator
import kg.ivy.wallet.domain.deprecated.logic.PlannedPaymentsLogic
import kg.ivy.wallet.domain.deprecated.logic.WalletAccountLogic
import kg.ivy.wallet.domain.deprecated.logic.WalletCategoryLogic
import kg.ivy.wallet.domain.pure.exchange.ExchangeData
import kg.ivy.wallet.ui.theme.modal.ChoosePeriodModalData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kg.ivy.legacy.datamodel.Account as LegacyAccount

@Stable
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val accountDao: AccountDao,
    private val categoryRepository: CategoryRepository,
    private val ivyContext: IvyWalletCtx,
    private val nav: Navigation,
    private val accountLogic: WalletAccountLogic,
    private val categoryLogic: WalletCategoryLogic,
    private val categoryCreator: CategoryCreator,
    private val accountCreator: AccountCreator,
    private val plannedPaymentsLogic: PlannedPaymentsLogic,
    private val sharedPrefs: SharedPrefs,
    private val accountsAct: AccountsAct,
    private val accTrnsAct: AccTrnsAct,
    private val trnsWithDateDivsAct: LegacyTrnsWithDateDivsAct,
    private val baseCurrencyAct: BaseCurrencyAct,
    private val calcAccBalanceAct: CalcAccBalanceAct,
    private val calcAccIncomeExpenseAct: CalcAccIncomeExpenseAct,
    private val calcTrnsIncomeExpenseAct: LegacyCalcTrnsIncomeExpenseAct,
    private val exchangeAct: ExchangeAct,
    private val transactionRepository: TransactionRepository,
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val transactionMapper: TransactionMapper,
    private val tagRepository: TagRepository,
    private val timeProvider: TimeProvider,
    private val timeConverter: TimeConverter,
    private val features: Features
) : ComposeViewModel<TransactionsState, TransactionsEvent>() {

    private val period = mutableStateOf(ivyContext.selectedPeriod)
    private val categories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val accounts = mutableStateOf<ImmutableList<LegacyAccount>>(persistentListOf())
    private val baseCurrency = mutableStateOf("")
    private val currency = mutableStateOf("")
    private val balance = mutableDoubleStateOf(0.0)
    private val balanceBaseCurrency = mutableStateOf<Double?>(null)
    private val income = mutableDoubleStateOf(0.0)
    private val expenses = mutableDoubleStateOf(0.0)

    // Upcoming
    private val upcoming = mutableStateOf<ImmutableList<Transaction>>(persistentListOf())
    private val upcomingIncome = mutableDoubleStateOf(0.0)
    private val upcomingExpenses = mutableDoubleStateOf(0.0)
    private val upcomingExpanded = mutableStateOf(false)

    // Overdue
    private val overdue = mutableStateOf<ImmutableList<Transaction>>(persistentListOf())
    private val overdueIncome = mutableDoubleStateOf(0.0)
    private val overdueExpenses = mutableDoubleStateOf(0.0)
    private val overdueExpanded = mutableStateOf(true)

    // History
    private val history =
        mutableStateOf<ImmutableList<TransactionHistoryItem>>(persistentListOf())

    private val account = mutableStateOf<LegacyAccount?>(null)
    private val category = mutableStateOf<Category?>(null)
    private val initWithTransactions = mutableStateOf(false)
    private val treatTransfersAsIncomeExpense = mutableStateOf(false)
    private val accountNameConfirmation = mutableStateOf(selectEndTextFieldValue(""))
    private val enableDeletionButton = mutableStateOf(false)
    private val skipAllModalVisible = mutableStateOf(false)
    private val deleteModal1Visible = mutableStateOf(false)
    private val choosePeriodModal = mutableStateOf<ChoosePeriodModalData?>(null)

    @Composable
    override fun uiState(): TransactionsState {
        return TransactionsState(
            period = getPeriod(),
            baseCurrency = getBaseCurrency(),
            currency = getCurrency(),
            categories = getCategories(),
            accounts = getAccounts(),
            account = getAccount(),
            category = getCategory(),
            balance = getBalance(),
            balanceBaseCurrency = getBalanceBaseCurrency(),
            income = getIncome(),
            expenses = getExpenses(),
            initWithTransactions = getInitWithTransactions(),
            treatTransfersAsIncomeExpense = getTreatTransfersAsIncomeExpense(),
            history = getHistory(),
            upcoming = getUpcoming(),
            upcomingExpanded = getUpcomingExpanded(),
            upcomingIncome = getUpcomingIncome(),
            upcomingExpenses = getUpcomingExpenses(),
            overdue = getOverdue(),
            overdueExpanded = getOverdueExpanded(),
            overdueIncome = getOverdueIncome(),
            overdueExpenses = getOverdueExpenses(),
            enableDeletionButton = getEnableDeletionButton(),
            skipAllModalVisible = getSkipAllModalVisible(),
            deleteModal1Visible = getDeleteModal1Visible(),
            choosePeriodModal = getChoosePeriodModal(),
            showAccountColorsInTransactions = getShouldShowAccountSpecificColorInTransactions()
        )
    }

    @Composable
    fun getShouldShowAccountSpecificColorInTransactions(): Boolean {
        return features.showAccountColorsInTransactions.asEnabledState()
    }

    @Composable
    private fun getPeriod(): TimePeriod {
        return period.value
    }

    @Composable
    private fun getBaseCurrency(): String {
        return baseCurrency.value
    }

    @Composable
    private fun getAccount(): LegacyAccount? {
        return account.value
    }

    @Composable
    private fun getCurrency(): String {
        return currency.value
    }

    @Composable
    private fun getCategories(): ImmutableList<Category> {
        return categories.value
    }

    @Composable
    private fun getAccounts(): ImmutableList<LegacyAccount> {
        return accounts.value
    }

    @Composable
    private fun getCategory(): Category? {
        return category.value
    }

    @Composable
    private fun getBalance(): Double {
        return balance.doubleValue
    }

    @Composable
    private fun getBalanceBaseCurrency(): Double? {
        return balanceBaseCurrency.value
    }

    @Composable
    private fun getIncome(): Double {
        return income.doubleValue
    }

    @Composable
    private fun getExpenses(): Double {
        return expenses.doubleValue
    }

    @Composable
    private fun getInitWithTransactions(): Boolean {
        return initWithTransactions.value
    }

    @Composable
    private fun getTreatTransfersAsIncomeExpense(): Boolean {
        return treatTransfersAsIncomeExpense.value
    }

    @Composable
    private fun getUpcomingExpenses(): Double {
        return upcomingExpenses.doubleValue
    }

    @Composable
    private fun getUpcoming(): ImmutableList<Transaction> {
        return upcoming.value
    }

    @Composable
    private fun getUpcomingExpanded(): Boolean {
        return upcomingExpanded.value
    }

    @Composable
    private fun getUpcomingIncome(): Double {
        return upcomingIncome.doubleValue
    }

    @Composable
    private fun getHistory(): ImmutableList<TransactionHistoryItem> {
        return history.value
    }

    @Composable
    private fun getOverdue(): ImmutableList<Transaction> {
        return overdue.value
    }

    @Composable
    private fun getOverdueExpanded(): Boolean {
        return overdueExpanded.value
    }

    @Composable
    private fun getOverdueIncome(): Double {
        return overdueIncome.doubleValue
    }

    @Composable
    private fun getOverdueExpenses(): Double {
        return overdueExpenses.doubleValue
    }

    @Composable
    private fun getEnableDeletionButton(): Boolean {
        return enableDeletionButton.value
    }

    @Composable
    private fun getSkipAllModalVisible(): Boolean {
        return skipAllModalVisible.value
    }

    @Composable
    private fun getDeleteModal1Visible(): Boolean {
        return deleteModal1Visible.value
    }

    @Composable
    private fun getChoosePeriodModal(): ChoosePeriodModalData? {
        return choosePeriodModal.value
    }

    override fun onEvent(event: TransactionsEvent) {
        when (event) {
            is TransactionsEvent.Delete -> delete(event.screen)
            is TransactionsEvent.EditAccount -> editAccount(
                event.screen,
                event.account,
                event.newBalance
            )

            is TransactionsEvent.EditCategory -> editCategory(event.updatedCategory)
            is TransactionsEvent.NextMonth -> nextMonth(event.screen)
            is TransactionsEvent.PayOrGet -> payOrGet(event.screen, event.transaction)
            is TransactionsEvent.PreviousMonth -> previousMonth(event.screen)
            is TransactionsEvent.SetPeriod -> setPeriod(event.screen, event.period)
            is TransactionsEvent.SkipTransaction -> skipTransaction(event.screen, event.transaction)
            is TransactionsEvent.SkipTransactions -> skipTransactions(
                event.screen,
                event.transactions
            )

            is TransactionsEvent.UpdateAccountDeletionState -> updateAccountDeletionState(
                event.confirmationText
            )

            is TransactionsEvent.SetOverdueExpanded -> setOverdueExpanded(event.expanded)
            is TransactionsEvent.SetUpcomingExpanded -> setUpcomingExpanded(event.expanded)
            is TransactionsEvent.SetSkipAllModalVisible -> setSkipAllModalVisible(event.visible)
            is TransactionsEvent.OnDeleteModal1Visible -> setDeleteModal1Visible(event.delete)
            is TransactionsEvent.OnChoosePeriodModalData -> setChoosePeriodModalData(event.data)
        }
    }

    private suspend fun initForAccount(accountId: UUID) {
        val initialAccount = ioThread {
            accountDao.findById(accountId)?.toLegacyDomain() ?: error("account not found")
        }
        account.value = initialAccount
        val range = period.value.toRange(ivyContext.startDayOfMonth, timeConverter, timeProvider)

        if (initialAccount.currency.isNotNullOrBlank()) {
            currency.value = initialAccount.currency!!
        }

        val account = accountRepository.findById(AccountId(accountId)) ?: error("account not found")

        val balanceValue = calcAccBalanceAct(
            CalcAccBalanceAct.Input(
                account = account
            )
        ).balance.toDouble()
        balance.doubleValue = balanceValue
        if (baseCurrency.value != currency.value) {
            balanceBaseCurrency.value = exchangeAct(
                ExchangeAct.Input(
                    data = ExchangeData(
                        baseCurrency = baseCurrency.value,
                        fromCurrency = currency.value.toOption()
                    ),
                    amount = balanceValue.toBigDecimal()
                )
            ).orNull()?.toDouble()
        }

        val includeTransfersInCalc =
            sharedPrefs.getBoolean(SharedPrefs.TRANSFERS_AS_INCOME_EXPENSE, false)

        val incomeExpensePair = calcAccIncomeExpenseAct(
            CalcAccIncomeExpenseAct.Input(
                account = account,
                range = range.toCloseTimeRange(),
                includeTransfersInCalc = includeTransfersInCalc
            )
        ).incomeExpensePair
        income.doubleValue = incomeExpensePair.income.toDouble()
        expenses.doubleValue = incomeExpensePair.expense.toDouble()

        history.value = (
                accTrnsAct then {
                    trnsWithDateDivsAct(
                        LegacyTrnsWithDateDivsAct.Input(
                            baseCurrency = baseCurrency.value,
                            transactions = with(transactionMapper) {
                                it.map {
                                    val tags =
                                        tagRepository.findByIds(it.tags).toImmutableLegacyTags()
                                    it.toEntity().toLegacyDomain(tags = tags)
                                }
                            }
                        )
                    )
                }
                )(
            AccTrnsAct.Input(
                accountId = initialAccount.id,
                range = range.toCloseTimeRange()
            )
        ).toImmutableList()

        // Upcoming
        upcomingIncome.doubleValue = ioThread {
            accountLogic.calculateUpcomingIncome(initialAccount, range)
        }

        upcomingExpenses.doubleValue = ioThread {
            accountLogic.calculateUpcomingExpenses(initialAccount, range)
        }

        upcoming.value = ioThread {
            with(transactionMapper) {
                accountLogic.upcoming(initialAccount, range).map { it.toEntity().toLegacyDomain() }
            }.toImmutableList()
        }

        // Overdue
        overdueIncome.doubleValue = ioThread {
            accountLogic.calculateOverdueIncome(initialAccount, range)
        }

        overdueExpenses.doubleValue = ioThread {
            accountLogic.calculateOverdueExpenses(initialAccount, range)
        }

        overdue.value = ioThread {
            with(transactionMapper) {
                accountLogic.overdue(initialAccount, range).map {
                    it.toEntity().toLegacyDomain()
                }.toImmutableList()
            }
        }
    }

    private suspend fun initForCategory(categoryId: UUID, accountFilterList: List<UUID>) {
        val accountFilterSet = accountFilterList.toSet()
        val initialCategory = ioThread {
            categoryRepository.findById(CategoryId(categoryId)) ?: error("category not found")
        }
        category.value = initialCategory
        val range = period.value.toRange(ivyContext.startDayOfMonth, timeConverter, timeProvider)

        balance.doubleValue = ioThread {
            categoryLogic.calculateCategoryBalance(initialCategory, range, accountFilterSet)
        }

        income.doubleValue = ioThread {
            categoryLogic.calculateCategoryIncome(initialCategory, range, accountFilterSet)
        }

        expenses.doubleValue = ioThread {
            categoryLogic.calculateCategoryExpenses(initialCategory, range, accountFilterSet)
        }

        history.value = ioThread {
            categoryLogic.historyByCategoryAccountWithDateDividers(
                initialCategory,
                range,
                accountFilterSet = accountFilterList.toSet(),
            ).toImmutableList()
        }

        // Upcoming
        // TODO: Rework Upcoming to FP
        upcomingIncome.doubleValue = ioThread {
            categoryLogic.calculateUpcomingIncomeByCategory(initialCategory, range)
        }

        upcomingExpenses.doubleValue = ioThread {
            categoryLogic.calculateUpcomingExpensesByCategory(initialCategory, range)
        }

        upcoming.value = ioThread {
            categoryLogic.upcomingByCategoryLegacy(initialCategory, range).toImmutableList()
        }

        // Overdue
        // TODO: Rework Overdue to FP
        overdueIncome.doubleValue = ioThread {
            categoryLogic.calculateOverdueIncomeByCategory(initialCategory, range)
        }

        overdueExpenses.doubleValue = ioThread {
            categoryLogic.calculateOverdueExpensesByCategory(initialCategory, range)
        }

        overdue.value =
            ioThread {
                categoryLogic.overdueByCategoryLegacy(initialCategory, range).toImmutableList()
            }
    }

    private suspend fun initForCategoryWithTransactions(
        categoryId: UUID,
        accountFilterList: List<UUID>,
        transactions: List<Transaction>,
    ) {
        computationThread {
            initWithTransactions.value = true

            val trans = transactions.filter {
                it.type != TransactionType.TRANSFER && it.categoryId == categoryId
            }

            val accountFilterSet = accountFilterList.toSet()
            val initialCategory = ioThread {
                categoryRepository.findById(CategoryId(categoryId)) ?: error("category not found")
            }
            category.value = initialCategory
            val range =
                period.value.toRange(ivyContext.startDayOfMonth, timeConverter, timeProvider)

            val incomeTrans = transactions.filter {
                it.categoryId == categoryId && it.type == TransactionType.INCOME
            }

            val expenseTrans = transactions.filter {
                it.categoryId == categoryId && it.type == TransactionType.EXPENSE
            }

            balance.value = ioThread {
                categoryLogic.calculateCategoryBalance(
                    initialCategory,
                    range,
                    accountFilterSet,
                    transactions = trans
                )
            }

            income.value = ioThread {
                categoryLogic.calculateCategoryIncome(
                    incomeTransaction = incomeTrans,
                    accountFilterSet = accountFilterSet
                )
            }

            expenses.doubleValue = ioThread {
                categoryLogic.calculateCategoryExpenses(
                    expenseTransactions = expenseTrans,
                    accountFilterSet = accountFilterSet
                )
            }

            history.value = ioThread {
                categoryLogic.historyByCategoryAccountWithDateDividers(
                    initialCategory,
                    range,
                    accountFilterSet = accountFilterList.toSet(),
                    transactions = trans
                ).toImmutableList()
            }

            // Upcoming
            // TODO: Rework Upcoming to FP
            upcomingIncome.doubleValue = ioThread {
                categoryLogic.calculateUpcomingIncomeByCategory(initialCategory, range)
            }

            upcomingExpenses.doubleValue = ioThread {
                categoryLogic.calculateUpcomingExpensesByCategory(initialCategory, range)
            }

            upcoming.value = ioThread {
                categoryLogic.upcomingByCategoryLegacy(initialCategory, range).toImmutableList()
            }

            // Overdue
            // TODO: Rework Overdue to FP
            overdueIncome.doubleValue = ioThread {
                categoryLogic.calculateOverdueIncomeByCategory(initialCategory, range)
            }

            overdueExpenses.doubleValue = ioThread {
                categoryLogic.calculateOverdueExpensesByCategory(initialCategory, range)
            }

            overdue.value =
                ioThread {
                    categoryLogic.overdueByCategoryLegacy(initialCategory, range).toImmutableList()
                }
        }
    }

    private suspend fun initForUnspecifiedCategory() {
        val range = period.value.toRange(ivyContext.startDayOfMonth, timeConverter, timeProvider)

        balance.doubleValue = ioThread {
            categoryLogic.calculateUnspecifiedBalance(range)
        }

        income.doubleValue = ioThread {
            categoryLogic.calculateUnspecifiedIncome(range)
        }

        expenses.doubleValue = ioThread {
            categoryLogic.calculateUnspecifiedExpenses(range)
        }

        history.value = ioThread {
            categoryLogic.historyUnspecified(range).toImmutableList()
        }

        // Upcoming
        upcomingIncome.doubleValue = ioThread {
            categoryLogic.calculateUpcomingIncomeUnspecified(range)
        }

        upcomingExpenses.value = ioThread {
            categoryLogic.calculateUpcomingExpensesUnspecified(range)
        }

        upcoming.value = ioThread {
            categoryLogic.upcomingUnspecifiedLegacy(range).toImmutableList()
        }

        // Overdue
        overdueIncome.doubleValue = ioThread {
            categoryLogic.calculateOverdueIncomeUnspecified(range)
        }

        overdueExpenses.doubleValue = ioThread {
            categoryLogic.calculateOverdueExpensesUnspecified(range)
        }

        overdue.value = ioThread { categoryLogic.overdueUnspecifiedLegacy(range).toImmutableList() }
    }

    private suspend fun initForAccountTransfersCategory(
        accountFilterList: List<UUID>,
        transactions: List<Transaction>,
    ) {
        initWithTransactions.value = true
        val accountTransferCategory = Category(
            name = NotBlankTrimmedString.unsafe(stringRes(R.string.account_transfers)),
            color = ColorInt(RedLight.toArgb()),
            icon = IconAsset.unsafe("transfer"),
            id = CategoryId(UUID.randomUUID()),
            orderNum = 0.0,
        )
        category.value = accountTransferCategory
        val accountFilterIdSet = accountFilterList.toHashSet()
        val trans = transactions.filter {
            it.categoryId == null && (
                    accountFilterIdSet.contains(it.accountId) || accountFilterIdSet.contains(
                        it.toAccountId
                    )
                    ) && it.type == TransactionType.TRANSFER
        }

        val historyIncomeExpense = calcTrnsIncomeExpenseAct(
            LegacyCalcTrnsIncomeExpenseAct.Input(
                transactions = trans,
                accounts = accountFilterList.mapNotNull { accID -> accounts.value.find { it.id == accID } },
                baseCurrency = baseCurrency.value
            )
        )

        income.doubleValue = historyIncomeExpense.transferIncome.toDouble()
        expenses.doubleValue = historyIncomeExpense.transferExpense.toDouble()
        balance.doubleValue = income.doubleValue - expenses.doubleValue
        history.value = trnsWithDateDivsAct(
            LegacyTrnsWithDateDivsAct.Input(
                baseCurrency = baseCurrency.value,
                transactions = transactions
            )
        ).toImmutableList()
    }

    private fun reset() {
        account.value = null
        category.value = null
    }

    private fun setUpcomingExpanded(expanded: Boolean) {
        upcomingExpanded.value = expanded
    }

    private fun setOverdueExpanded(expanded: Boolean) {
        overdueExpanded.value = expanded
    }

    private fun setPeriod(
        screen: TransactionsScreen,
        period: TimePeriod,
    ) {
        start(
            screen = screen,
            timePeriod = period,
            reset = false
        )
    }

    private fun setSkipAllModalVisible(visible: Boolean) {
        skipAllModalVisible.value = visible
    }

    private fun nextMonth(screen: TransactionsScreen) {
        val month = period.value.month
        val year = period.value.year ?: dateNowUTC().year
        if (month != null) {
            start(
                screen = screen,
                timePeriod = month.incrementMonthPeriod(ivyContext, 1L, year),
                reset = false
            )
        }
    }

    private fun previousMonth(screen: TransactionsScreen) {
        val month = period.value.month
        val year = period.value.year ?: dateNowUTC().year
        if (month != null) {
            start(
                screen = screen,
                timePeriod = month.incrementMonthPeriod(ivyContext, -1L, year),
                reset = false
            )
        }
    }

    private fun delete(screen: TransactionsScreen) {
        viewModelScope.launch {
            when {
                screen.accountId != null -> {
                    deleteAccount(screen.accountId!!)
                }

                screen.categoryId != null -> {
                    deleteCategory(screen.categoryId!!)
                }
            }
        }
    }

    private suspend fun deleteAccount(accountId: UUID) {
        ioThread {
            transactionRepository.deleteAllByAccountId(accountId = AccountId(accountId))
            plannedPaymentRuleWriter.deletedByAccountId(accountId = accountId)
            accountRepository.deleteById(AccountId(accountId))

            nav.back()
        }
    }

    private suspend fun deleteCategory(categoryId: UUID) {
        ioThread {
            categoryRepository.deleteById(CategoryId(categoryId))

            nav.back()
        }
    }

    private fun setDeleteModal1Visible(delete: Boolean) {
        deleteModal1Visible.value = delete
    }

    private fun setChoosePeriodModalData(data: ChoosePeriodModalData?) {
        choosePeriodModal.value = data
    }

    private fun editCategory(updatedCategory: Category) {
        viewModelScope.launch {
            categoryCreator.editCategory(updatedCategory) {
                category.value = it
            }
        }
    }

    private fun editAccount(
        screen: TransactionsScreen,
        account: LegacyAccount,
        newBalance: Double,
    ) {
        viewModelScope.launch {
            accountCreator.editAccount(account, newBalance) {
                start(
                    screen = screen,
                    timePeriod = period.value,
                    reset = false
                )
            }
        }
    }

    private fun payOrGet(screen: TransactionsScreen, transaction: Transaction) {
        viewModelScope.launch {
            plannedPaymentsLogic.payOrGetLegacy(transaction = transaction) {
                start(
                    screen = screen,
                    reset = false
                )
            }
        }
    }

    private fun skipTransaction(screen: TransactionsScreen, transaction: Transaction) {
        viewModelScope.launch {
            plannedPaymentsLogic.payOrGetLegacy(
                transaction = transaction,
                skipTransaction = true
            ) {
                start(
                    screen = screen,
                    reset = false
                )
            }
        }
    }

    private fun skipTransactions(screen: TransactionsScreen, transactions: List<Transaction>) {
        viewModelScope.launch {
            plannedPaymentsLogic.payOrGetLegacy(
                transactions = transactions,
                skipTransaction = true
            ) {
                start(
                    screen = screen,
                    reset = false
                )
            }
        }
    }

    private fun updateAccountDeletionState(confirmationText: String) {
        accountNameConfirmation.value = selectEndTextFieldValue(confirmationText)
        enableDeletionButton.value = account.value?.name == confirmationText ||
                category.value?.name?.value == confirmationText
    }

    fun start(
        screen: TransactionsScreen,
        timePeriod: TimePeriod? = ivyContext.selectedPeriod,
        reset: Boolean = true,
    ) {
        if (reset) {
            reset()
        }

        viewModelScope.launch {
            period.value = timePeriod ?: ivyContext.selectedPeriod

            val baseCurrencyValue = baseCurrencyAct(Unit)
            baseCurrency.value = baseCurrencyValue
            currency.value = baseCurrency.value

            categories.value = categoryRepository.findAll().toImmutableList()
            accounts.value = accountsAct(Unit)
            initWithTransactions.value = false
            treatTransfersAsIncomeExpense.value =
                sharedPrefs.getBoolean(SharedPrefs.TRANSFERS_AS_INCOME_EXPENSE, false)

            when {
                screen.accountId != null -> {
                    initForAccount(screen.accountId!!)
                }

                screen.categoryId != null && screen.transactions.isEmpty() -> {
                    initForCategory(screen.categoryId!!, screen.accountIdFilterList)
                }
                // unspecifiedCategory==false is explicitly checked to accommodate for a temp
                // AccountTransfers Category during Reports Screen
                screen.categoryId != null && screen.transactions.isNotEmpty() &&
                        screen.unspecifiedCategory == false -> {
                    initForCategoryWithTransactions(
                        screen.categoryId!!,
                        screen.accountIdFilterList,
                        screen.transactions
                    )
                }

                screen.unspecifiedCategory == true && screen.transactions.isNotEmpty() -> {
                    initForAccountTransfersCategory(
                        screen.accountIdFilterList,
                        screen.transactions
                    )
                }

                screen.unspecifiedCategory == true -> {
                    initForUnspecifiedCategory()
                }

                else -> error("no id provided")
            }
        }
    }
}
