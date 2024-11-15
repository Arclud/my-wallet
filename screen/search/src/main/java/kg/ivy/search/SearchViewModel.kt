package kg.ivy.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kg.ivy.base.legacy.TransactionHistoryItem
import kg.ivy.data.model.primitive.NotBlankTrimmedString
import kg.ivy.ui.ComposeViewModel
import kg.ivy.data.model.Category
import kg.ivy.data.repository.CategoryRepository
import kg.ivy.domain.features.Features
import kg.ivy.legacy.datamodel.Account
import kg.ivy.legacy.utils.getDefaultFIATCurrency
import kg.ivy.legacy.utils.ioThread
import kg.ivy.wallet.domain.action.account.AccountsAct
import kg.ivy.wallet.domain.action.settings.BaseCurrencyAct
import kg.ivy.wallet.domain.action.transaction.AllTrnsAct
import kg.ivy.wallet.domain.action.transaction.TrnsWithDateDivsAct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val trnsWithDateDivsAct: TrnsWithDateDivsAct,
    private val accountsAct: AccountsAct,
    private val categoryRepository: CategoryRepository,
    private val baseCurrencyAct: BaseCurrencyAct,
    private val allTrnsAct: AllTrnsAct,
    private val features: Features
) : ComposeViewModel<SearchState, SearchEvent>() {

    private val transactions =
        mutableStateOf<ImmutableList<TransactionHistoryItem>>(persistentListOf())
    private val baseCurrency = mutableStateOf<String>(getDefaultFIATCurrency().currencyCode)
    private val accounts = mutableStateOf<ImmutableList<Account>>(persistentListOf())
    private val categories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val searchQuery = mutableStateOf("")

    @Composable
    fun getShouldShowAccountSpecificColorInTransactions(): Boolean {
        return features.showAccountColorsInTransactions.asEnabledState()
    }

    @Composable
    override fun uiState(): SearchState {
        LaunchedEffect(Unit) {
            search(searchQuery.value)
        }

        return SearchState(
            searchQuery = searchQuery.value,
            transactions = transactions.value,
            baseCurrency = baseCurrency.value,
            accounts = accounts.value,
            categories = categories.value,
            shouldShowAccountSpecificColorInTransactions = getShouldShowAccountSpecificColorInTransactions()
        )
    }

    override fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.Search -> search(event.query)
        }
    }

    private fun search(query: String) {
        searchQuery.value = query
        val normalizedQuery = query.lowercase().trim()

        viewModelScope.launch {
            val queryResult = ioThread {
                val filteredTransactions = allTrnsAct(Unit)
                    .filter { transaction ->
                        transaction.title.matchesQuery(normalizedQuery) ||
                                transaction.description.matchesQuery(normalizedQuery)
                    }
                trnsWithDateDivsAct(
                    TrnsWithDateDivsAct.Input(
                        baseCurrency = baseCurrencyAct(Unit),
                        transactions = filteredTransactions
                    )
                ).toImmutableList()
            }

            transactions.value = queryResult
            baseCurrency.value = baseCurrencyAct(Unit)
            accounts.value = accountsAct(Unit)
            categories.value = categoryRepository.findAll().toImmutableList()
        }
    }

    private fun NotBlankTrimmedString?.matchesQuery(query: String): Boolean {
        return this?.value?.lowercase()?.contains(query) == true
    }
}
