package kg.ivy.wallet.domain.action.category

import kg.ivy.data.model.Category
import kg.ivy.data.model.Transaction
import kg.ivy.data.temp.migration.getAccountId
import kg.ivy.frp.action.FPAction
import kg.ivy.frp.then
import kg.ivy.legacy.datamodel.Account
import kg.ivy.wallet.domain.action.transaction.CalcTrnsIncomeExpenseAct
import kg.ivy.wallet.domain.action.transaction.LegacyCalcTrnsIncomeExpenseAct
import kg.ivy.wallet.domain.pure.data.IncomeExpenseTransferPair
import javax.inject.Inject

class CategoryIncomeWithAccountFiltersAct @Inject constructor(
    private val calcTrnsIncomeExpenseAct: CalcTrnsIncomeExpenseAct
) : FPAction<CategoryIncomeWithAccountFiltersAct.Input, IncomeExpenseTransferPair>() {

    override suspend fun Input.compose(): suspend () -> IncomeExpenseTransferPair = suspend {
        val accountFilterSet = accountFilterList.map { it.id }.toHashSet()
        transactions.filter {
            it.category?.value == category?.id?.value
        }.filter {
            if (accountFilterSet.isEmpty()) {
                true
            } else {
                accountFilterSet.contains(it.getAccountId())
            }
        }
    } then {
        CalcTrnsIncomeExpenseAct.Input(
            transactions = it,
            baseCurrency = baseCurrency,
            accounts = accountFilterList
        )
    } then calcTrnsIncomeExpenseAct

    data class Input(
        val transactions: List<Transaction>,
        val accountFilterList: List<Account>,
        val category: Category?,
        val baseCurrency: String
    )
}

@Deprecated("Uses legacy Transaction")
class LegacyCategoryIncomeWithAccountFiltersAct @Inject constructor(
    private val calcTrnsIncomeExpenseAct: LegacyCalcTrnsIncomeExpenseAct
) : FPAction<LegacyCategoryIncomeWithAccountFiltersAct.Input, IncomeExpenseTransferPair>() {

    override suspend fun Input.compose(): suspend () -> IncomeExpenseTransferPair = suspend {
        val accountFilterSet = accountFilterList.map { it.id }.toHashSet()
        transactions.filter {
            it.categoryId == category?.id?.value
        }.filter {
            if (accountFilterSet.isEmpty()) {
                true
            } else {
                accountFilterSet.contains(it.accountId)
            }
        }
    } then {
        LegacyCalcTrnsIncomeExpenseAct.Input(
            transactions = it,
            baseCurrency = baseCurrency,
            accounts = accountFilterList
        )
    } then calcTrnsIncomeExpenseAct

    data class Input(
        val transactions: List<kg.ivy.base.legacy.Transaction>,
        val accountFilterList: List<Account>,
        val category: Category?,
        val baseCurrency: String
    )
}
