package kg.ivy.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import kg.ivy.data.model.Category
import kg.ivy.legacy.data.model.AccountBalance
import kg.ivy.legacy.utils.onScreenStart
import kg.ivy.navigation.OnboardingScreen
import kg.ivy.onboarding.steps.OnboardingAccounts
import kg.ivy.onboarding.steps.OnboardingCategories
import kg.ivy.onboarding.steps.OnboardingSetCurrency
import kg.ivy.onboarding.steps.OnboardingSplashLogin
import kg.ivy.onboarding.steps.OnboardingType
import kg.ivy.onboarding.viewmodel.OnboardingViewModel
import kg.ivy.wallet.domain.data.IvyCurrency
import kg.ivy.wallet.domain.deprecated.logic.model.CreateAccountData
import kg.ivy.wallet.domain.deprecated.logic.model.CreateCategoryData
import kotlinx.collections.immutable.ImmutableList

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.OnboardingScreen(screen: OnboardingScreen) {
    val viewModel: OnboardingViewModel = viewModel()

    val state by viewModel.state
    val uiState = viewModel.uiState()

    val isSystemDarkTheme = isSystemInDarkTheme()
    onScreenStart {
        viewModel.start(
            screen = screen,
            isSystemDarkMode = isSystemDarkTheme
        )
    }

    UI(
        onboardingState = state,
        currency = uiState.currency,

        accountSuggestions = uiState.accountSuggestions,
        accounts = uiState.accounts,

        categorySuggestions = uiState.categorySuggestions,
        categories = uiState.categories,

        onEvent = viewModel::onEvent

    )
}

@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    onboardingState: OnboardingState,
    currency: IvyCurrency,

    accountSuggestions: ImmutableList<CreateAccountData>,
    accounts: ImmutableList<AccountBalance>,

    categorySuggestions: ImmutableList<CreateCategoryData>,
    categories: ImmutableList<Category>,

    onEvent: (OnboardingEvent) -> Unit = {}
) {
    when (onboardingState) {
        OnboardingState.SPLASH, OnboardingState.LOGIN -> {
            OnboardingSplashLogin(
                onboardingState = onboardingState,
                onSkip = { onEvent(OnboardingEvent.LoginOfflineAccount) }
            )
        }

        OnboardingState.CHOOSE_PATH -> {
            OnboardingType(
                onStartImport = { onEvent(OnboardingEvent.StartImport) },
                onStartFresh = { onEvent(OnboardingEvent.StartFresh) }
            )
        }

        OnboardingState.CURRENCY -> {
            OnboardingSetCurrency(
                preselectedCurrency = currency,
                onSetCurrency = { onEvent(OnboardingEvent.SetBaseCurrency(it)) }
            )
        }

        OnboardingState.ACCOUNTS -> {
            OnboardingAccounts(
                baseCurrency = currency.code,
                suggestions = accountSuggestions,
                accounts = accounts,

                onCreateAccount = { onEvent(OnboardingEvent.CreateAccount(it)) },
                onEditAccount = { account, newBalance ->
                    onEvent(
                        OnboardingEvent.EditAccount(
                            account,
                            newBalance
                        )
                    )
                },

                onDoneClick = { onEvent(OnboardingEvent.OnAddAccountsDone) },
                onSkip = { onEvent(OnboardingEvent.OnAddAccountsSkip) }
            )
        }

        OnboardingState.CATEGORIES -> {
            OnboardingCategories(
                suggestions = categorySuggestions,
                categories = categories,

                onCreateCategory = { onEvent(OnboardingEvent.CreateCategory(it)) },
                onEditCategory = { onEvent(OnboardingEvent.EditCategory(it)) },

                onDoneClick = { onEvent(OnboardingEvent.OnAddCategoriesDone) },
                onSkip = { onEvent(OnboardingEvent.OnAddCategoriesSkip) }
            )
        }
    }
}
