package kg.ivy.onboarding

import androidx.compose.runtime.Immutable
import kg.ivy.data.model.Category
import kg.ivy.legacy.data.model.AccountBalance
import kg.ivy.wallet.domain.data.IvyCurrency
import kg.ivy.wallet.domain.deprecated.logic.model.CreateAccountData
import kg.ivy.wallet.domain.deprecated.logic.model.CreateCategoryData
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class OnboardingDetailState(
    val currency: IvyCurrency,
    val accounts: ImmutableList<AccountBalance>,
    val accountSuggestions: ImmutableList<CreateAccountData>,
    val categories: ImmutableList<Category>,
    val categorySuggestions: ImmutableList<CreateCategoryData>
)
