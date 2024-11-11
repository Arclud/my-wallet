package kg.ivy

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import kg.ivy.attributions.AttributionsScreenImpl
import kg.ivy.balance.BalanceScreen
import kg.ivy.budgets.BudgetScreen
import kg.ivy.categories.CategoriesScreen
import kg.ivy.contributors.ContributorsScreenImpl
import kg.ivy.disclaimer.DisclaimerScreenImpl
import kg.ivy.exchangerates.ExchangeRatesScreen
import kg.ivy.features.FeaturesScreenImpl
import kg.ivy.importdata.csv.CSVScreen
import kg.ivy.importdata.csvimport.ImportCSVScreen
import kg.ivy.loans.loan.LoansScreen
import kg.ivy.loans.loandetails.LoanDetailsScreen
import kg.ivy.main.MainScreen
import kg.ivy.navigation.AttributionsScreen
import kg.ivy.navigation.BalanceScreen
import kg.ivy.navigation.BudgetScreen
import kg.ivy.navigation.CSVScreen
import kg.ivy.navigation.CategoriesScreen
import kg.ivy.navigation.ContributorsScreen
import kg.ivy.navigation.DisclaimerScreen
import kg.ivy.navigation.EditPlannedScreen
import kg.ivy.navigation.EditTransactionScreen
import kg.ivy.navigation.ExchangeRatesScreen
import kg.ivy.navigation.FeaturesScreen
import kg.ivy.navigation.ImportScreen
import kg.ivy.navigation.LoanDetailsScreen
import kg.ivy.navigation.LoansScreen
import kg.ivy.navigation.MainScreen
import kg.ivy.navigation.OnboardingScreen
import kg.ivy.navigation.PieChartStatisticScreen
import kg.ivy.navigation.PlannedPaymentsScreen
import kg.ivy.navigation.ReleasesScreen
import kg.ivy.navigation.ReportScreen
import kg.ivy.navigation.Screen
import kg.ivy.navigation.SearchScreen
import kg.ivy.navigation.SettingsScreen
import kg.ivy.navigation.TransactionsScreen
import kg.ivy.onboarding.OnboardingScreen
import kg.ivy.piechart.PieChartStatisticScreen
import kg.ivy.planned.edit.EditPlannedScreen
import kg.ivy.planned.list.PlannedPaymentsScreen
import kg.ivy.releases.ReleasesScreenImpl
import kg.ivy.reports.ReportScreen
import kg.ivy.search.SearchScreen
import kg.ivy.settings.SettingsScreen
import kg.ivy.transaction.EditTransactionScreen
import kg.ivy.transactions.TransactionsScreen

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
@Suppress("CyclomaticComplexMethod", "FunctionNaming")
fun BoxWithConstraintsScope.IvyNavGraph(screen: Screen?) {
    when (screen) {
        null -> {
            // show nothing
        }

        is MainScreen -> MainScreen(screen = screen)
        is OnboardingScreen -> OnboardingScreen(screen = screen)
        is ExchangeRatesScreen -> ExchangeRatesScreen()
        is EditTransactionScreen -> EditTransactionScreen(screen = screen)
        is TransactionsScreen -> TransactionsScreen(screen = screen)
        is PieChartStatisticScreen -> PieChartStatisticScreen(screen = screen)
        is CategoriesScreen -> CategoriesScreen(screen = screen)
        is SettingsScreen -> SettingsScreen()
        is PlannedPaymentsScreen -> PlannedPaymentsScreen(screen = screen)
        is EditPlannedScreen -> EditPlannedScreen(screen = screen)
        is BalanceScreen -> BalanceScreen(screen = screen)
        is ImportScreen -> ImportCSVScreen(screen = screen)
        is ReportScreen -> ReportScreen(screen = screen)
        is BudgetScreen -> BudgetScreen(screen = screen)
        is LoansScreen -> LoansScreen(screen = screen)
        is LoanDetailsScreen -> LoanDetailsScreen(screen = screen)
        is SearchScreen -> SearchScreen(screen = screen)
        is CSVScreen -> CSVScreen(screen = screen)
        FeaturesScreen -> FeaturesScreenImpl()
        AttributionsScreen -> AttributionsScreenImpl()
        ContributorsScreen -> ContributorsScreenImpl()
        ReleasesScreen -> ReleasesScreenImpl()
        DisclaimerScreen -> DisclaimerScreenImpl()
    }
}
