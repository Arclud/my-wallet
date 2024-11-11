package kg.ivy.onboarding.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kg.ivy.base.legacy.SharedPrefs
import kg.ivy.base.legacy.Theme
import kg.ivy.data.db.dao.read.AccountDao
import kg.ivy.data.db.dao.read.SettingsDao
import kg.ivy.data.db.dao.write.WriteSettingsDao
import kg.ivy.ui.ComposeViewModel
import kg.ivy.domain.usecase.exchange.SyncExchangeRatesUseCase
import kg.ivy.legacy.LogoutLogic
import kg.ivy.data.model.Category
import kg.ivy.data.repository.CategoryRepository
import kg.ivy.legacy.data.model.AccountBalance
import kg.ivy.legacy.datamodel.Account
import kg.ivy.legacy.datamodel.Settings
import kg.ivy.legacy.domain.deprecated.logic.AccountCreator
import kg.ivy.legacy.utils.OpResult
import kg.ivy.legacy.utils.ioThread
import kg.ivy.navigation.Navigation
import kg.ivy.navigation.OnboardingScreen
import kg.ivy.onboarding.OnboardingDetailState
import kg.ivy.onboarding.OnboardingEvent
import kg.ivy.onboarding.OnboardingState
import kg.ivy.wallet.domain.action.account.AccountsAct
import kg.ivy.wallet.domain.data.IvyCurrency
import kg.ivy.wallet.domain.deprecated.logic.CategoryCreator
import kg.ivy.wallet.domain.deprecated.logic.PreloadDataLogic
import kg.ivy.wallet.domain.deprecated.logic.WalletAccountLogic
import kg.ivy.wallet.domain.deprecated.logic.model.CreateAccountData
import kg.ivy.wallet.domain.deprecated.logic.model.CreateCategoryData
import kg.ivy.wallet.domain.deprecated.logic.notification.TransactionReminderLogic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val nav: Navigation,
    private val accountDao: AccountDao,
    private val settingsDao: SettingsDao,
    private val accountLogic: WalletAccountLogic,
    private val categoryCreator: CategoryCreator,
    private val categoryRepository: CategoryRepository,
    private val accountCreator: AccountCreator,

    private val accountsAct: AccountsAct,
    private val syncExchangeRatesUseCase: SyncExchangeRatesUseCase,
    private val settingsWriter: WriteSettingsDao,

    // Only OnboardingRouter stuff
    sharedPrefs: SharedPrefs,
    transactionReminderLogic: TransactionReminderLogic,
    preloadDataLogic: PreloadDataLogic,
    logoutLogic: LogoutLogic,
) : ComposeViewModel<OnboardingDetailState, OnboardingEvent>() {

    private val _state = mutableStateOf(OnboardingState.SPLASH)
    val state: State<OnboardingState> = _state

    private val _currency = mutableStateOf(IvyCurrency.getDefault())
    private val _opGoogleSignIn = mutableStateOf<OpResult<Unit>?>(null)
    private val _accounts = mutableStateOf(listOf<AccountBalance>().toImmutableList())
    private val _accountSuggestions = mutableStateOf(listOf<CreateAccountData>().toImmutableList())
    private val _categories = mutableStateOf(listOf<Category>().toImmutableList())
    private val _categorySuggestions =
        mutableStateOf(listOf<CreateCategoryData>().toImmutableList())

    @Composable
    override fun uiState(): OnboardingDetailState {
        return OnboardingDetailState(
            currency = _currency.value,
            accounts = _accounts.value,
            accountSuggestions = _accountSuggestions.value,
            categories = _categories.value,
            categorySuggestions = _categorySuggestions.value
        )
    }

    private val router = OnboardingRouter(
        state = _state,
        opGoogleSignIn = _opGoogleSignIn,
        accounts = _accounts,
        accountSuggestions = _accountSuggestions,
        categories = _categories,
        categorySuggestions = _categorySuggestions,

        nav = nav,
        accountDao = accountDao,
        sharedPrefs = sharedPrefs,
        categoryRepository = categoryRepository,
        preloadDataLogic = preloadDataLogic,
        transactionReminderLogic = transactionReminderLogic,
        logoutLogic = logoutLogic,
        syncExchangeRatesUseCase = syncExchangeRatesUseCase,
    )

    fun start(screen: OnboardingScreen, isSystemDarkMode: Boolean) {
        viewModelScope.launch {
            initiateSettings(isSystemDarkMode)

            router.initBackHandling(
                screen = screen,
                viewModelScope = viewModelScope,
                restartOnboarding = {
                    start(screen, isSystemDarkMode)
                }
            )

            router.splashNext()
        }
    }

    private suspend fun initiateSettings(isSystemDarkMode: Boolean) {
        val defaultCurrency = IvyCurrency.getDefault()
        _currency.value = defaultCurrency

        ioThread {
            if (settingsDao.findAll().isEmpty()) {
                settingsWriter.save(
                    Settings(
                        theme = if (isSystemDarkMode) Theme.DARK else Theme.LIGHT,
                        name = "",
                        baseCurrency = defaultCurrency.code,
                        bufferAmount = 1000.0.toBigDecimal()
                    ).toEntity()
                )
            }
        }
    }

    override fun onEvent(event: OnboardingEvent) {
        viewModelScope.launch {
            when (event) {
                is OnboardingEvent.CreateAccount -> createAccount(event.data)
                is OnboardingEvent.CreateCategory -> createCategory(event.data)
                is OnboardingEvent.EditAccount -> editAccount(event.account, event.newBalance)
                is OnboardingEvent.EditCategory -> editCategory(event.updatedCategory)
                is OnboardingEvent.ImportFinished -> importFinished(event.success)
                OnboardingEvent.ImportSkip -> importSkip()
                OnboardingEvent.LoginOfflineAccount -> loginOfflineAccount()
                OnboardingEvent.OnAddAccountsDone -> onAddAccountsDone()
                OnboardingEvent.OnAddAccountsSkip -> onAddAccountsSkip()
                OnboardingEvent.OnAddCategoriesDone -> onAddCategoriesDone()
                OnboardingEvent.OnAddCategoriesSkip -> onAddCategoriesSkip()
                is OnboardingEvent.SetBaseCurrency -> setBaseCurrency(event.baseCurrency)
                OnboardingEvent.StartFresh -> startFresh()
                OnboardingEvent.StartImport -> startImport()
            }
        }
    }

    private suspend fun loginOfflineAccount() {
        router.offlineAccountNext()
    }
    // Step 1 ---------------------------------------------------------------------------------------

    // Step 2 ---------------------------------------------------------------------------------------
    private fun startImport() {
        router.startImport()
    }

    fun importSkip() {
        router.importSkip()
    }

    fun importFinished(success: Boolean) {
        router.importFinished(success)
    }

    private fun startFresh() {
        router.startFresh()
    }
    // Step 2 ---------------------------------------------------------------------------------------

    private suspend fun setBaseCurrency(baseCurrency: IvyCurrency) {
        updateBaseCurrency(baseCurrency)
        router.setBaseCurrencyNext(
            baseCurrency = baseCurrency,
            accountsWithBalance = { accountsWithBalance() }
        )
    }

    private suspend fun updateBaseCurrency(baseCurrency: IvyCurrency) {
        ioThread {
            settingsWriter.save(
                settingsDao.findFirst().copy(
                    currency = baseCurrency.code
                )
            )
        }
        _currency.value = baseCurrency
    }

    // --------------------- Accounts ---------------------------------------------------------------
    private suspend fun editAccount(account: Account, newBalance: Double) {
        accountCreator.editAccount(account, newBalance) {
            _accounts.value = accountsWithBalance()
        }
    }

    private suspend fun createAccount(data: CreateAccountData) {
        accountCreator.createAccount(data) {
            _accounts.value = accountsWithBalance()
        }
    }

    private suspend fun accountsWithBalance(): ImmutableList<AccountBalance> = ioThread {
        accountsAct(Unit)
            .map {
                AccountBalance(
                    account = it,
                    balance = ioThread { accountLogic.calculateAccountBalance(it) }
                )
            }.toImmutableList()
    }

    private suspend fun onAddAccountsDone() {
        router.accountsNext()
    }

    private suspend fun onAddAccountsSkip() {
        router.accountsSkip()
    }
    // --------------------- Accounts ---------------------------------------------------------------

    // ---------------------------- Categories ------------------------------------------------------
    private suspend fun editCategory(updatedCategory: Category) {
        categoryCreator.editCategory(updatedCategory) {
            _categories.value = categoryRepository.findAll().toImmutableList()
        }
    }

    private suspend fun createCategory(data: CreateCategoryData) {
        categoryCreator.createCategory(data) {
            _categories.value = categoryRepository.findAll().toImmutableList()
        }
    }

    private suspend fun onAddCategoriesDone() {
        router.categoriesNext(baseCurrency = _currency.value)
    }

    private suspend fun onAddCategoriesSkip() {
        router.categoriesSkip(baseCurrency = _currency.value)
    }
    // ---------------------------- Categories ------------------------------------------------------
}
