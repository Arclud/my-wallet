package kg.ivy.budgets

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kg.ivy.budgets.model.DisplayBudget
import kg.ivy.design.api.LocalTimeFormatter
import kg.ivy.design.l0_system.UI
import kg.ivy.design.l0_system.style
import kg.ivy.legacy.legacy.ui.theme.components.BudgetBattery
import kg.ivy.legacy.utils.clickableNoIndication
import kg.ivy.legacy.utils.format
import kg.ivy.legacy.utils.rememberInteractionSource
import kg.ivy.navigation.BudgetScreen
import kg.ivy.navigation.navigation
import kg.ivy.navigation.screenScopedViewModel
import kg.ivy.ui.R
import kg.ivy.wallet.ui.theme.Gray
import kg.ivy.wallet.ui.theme.components.IvyIcon
import kg.ivy.wallet.ui.theme.components.ReorderButton
import kg.ivy.wallet.ui.theme.components.ReorderModalSingleType
import kg.ivy.wallet.ui.theme.wallet.AmountCurrencyB1

@Composable
fun BoxWithConstraintsScope.BudgetScreen(screen: BudgetScreen) {
    val viewModel: BudgetViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    UI(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: BudgetScreenState,
    onEvent: (BudgetScreenEvent) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(32.dp))

        Toolbar(
            timeRange = state.timeRange,
            totalRemainingBudgetText = state.totalRemainingBudgetText,
            baseCurrency = state.baseCurrency,
            appBudgetMax = state.appBudgetMax,
            categoryBudgetsTotal = state.categoryBudgetsTotal,
            setReorderModalVisible = {
                onEvent(BudgetScreenEvent.OnReorderModalVisible(it))
            }
        )

        Spacer(Modifier.height(8.dp))

        for (item in state.budgets) {
            Spacer(Modifier.height(24.dp))

            BudgetItem(
                displayBudget = item,
                baseCurrency = state.baseCurrency
            ) {
                onEvent(
                    BudgetScreenEvent.OnBudgetModalData(
                        BudgetModalData(
                            budget = item.budget,
                            baseCurrency = state.baseCurrency,
                            categories = state.categories,
                            accounts = state.accounts,
                            autoFocusKeyboard = false
                        )
                    )
                )
            }
        }

        if (state.budgets.isEmpty()) {
            Spacer(Modifier.weight(1f))

            NoBudgetsEmptyState(
                emptyStateTitle = stringResource(R.string.no_budgets),
                emptyStateText = stringResource(R.string.no_budgets_text)
            )

            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(150.dp)) // scroll hack
    }

    val nav = navigation()
    BudgetBottomBar(
        onAdd = {
            onEvent(
                BudgetScreenEvent.OnBudgetModalData(
                    BudgetModalData(
                        budget = null,
                        baseCurrency = state.baseCurrency,
                        categories = state.categories,
                        accounts = state.accounts
                    )
                )
            )
        },
        onClose = {
            nav.back()
        },
    )

    ReorderModalSingleType(
        visible = state.reorderModalVisible,
        initialItems = state.budgets,
        dismiss = {
            onEvent(BudgetScreenEvent.OnReorderModalVisible(false))
        },
        onReordered = { onEvent(BudgetScreenEvent.OnReorder(it)) }
    ) { _, item ->
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp)
                .padding(vertical = 8.dp),
            text = item.budget.name,
            style = UI.typo.b1.style(
                color = UI.colors.pureInverse,
                fontWeight = FontWeight.Bold
            )
        )
    }

    BudgetModal(
        modal = state.budgetModalData,
        onCreate = { onEvent(BudgetScreenEvent.OnCreateBudget(it)) },
        onEdit = { onEvent(BudgetScreenEvent.OnEditBudget(it)) },
        onDelete = { onEvent(BudgetScreenEvent.OnDeleteBudget(it)) },
        dismiss = {
            onEvent(BudgetScreenEvent.OnBudgetModalData(null))
        }
    )
}

@Composable
private fun Toolbar(
    timeRange: kg.ivy.legacy.data.model.FromToTimeRange?,
    totalRemainingBudgetText: String?,
    baseCurrency: String,
    appBudgetMax: Double,
    categoryBudgetsTotal: Double,
    setReorderModalVisible: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp, end = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.budgets),
                style = UI.typo.h2.style(
                    color = UI.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            if (timeRange != null) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = timeRange.toDisplay(LocalTimeFormatter.current),
                    style = UI.typo.b2.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            if (categoryBudgetsTotal > 0 || appBudgetMax > 0) {
                Spacer(Modifier.height(4.dp))

                val categoryBudgetText = if (categoryBudgetsTotal > 0) {
                    stringResource(
                        R.string.for_categories,
                        categoryBudgetsTotal.format(baseCurrency),
                        baseCurrency
                    )
                } else {
                    ""
                }

                val appBudgetMaxText = if (appBudgetMax > 0) {
                    stringResource(
                        R.string.app_budget,
                        appBudgetMax.format(baseCurrency),
                        baseCurrency
                    )
                } else {
                    ""
                }

                val hasBothBudgetTypes =
                    categoryBudgetText.isNotBlank() && appBudgetMaxText.isNotBlank()
                Text(
                    modifier = Modifier.testTag("budgets_info_text"),
                    text = if (hasBothBudgetTypes) {
                        stringResource(
                            R.string.budget_info_both,
                            categoryBudgetText,
                            appBudgetMaxText
                        )
                    } else {
                        stringResource(R.string.budget_info, categoryBudgetText, appBudgetMaxText)
                    },
                    style = UI.typo.nC.style(
                        color = Gray,
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                if (totalRemainingBudgetText != null) {
                    Text(
                        text = totalRemainingBudgetText,
                        style = UI.typo.nC.style(
                            color = Gray,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }

        ReorderButton {
            setReorderModalVisible(true)
        }

        Spacer(Modifier.width(24.dp))
    }
}

@SuppressLint("ComposeContentEmitterReturningValues", "ComposeMultipleContentEmitters")
@Composable
private fun BudgetItem(
    displayBudget: DisplayBudget,
    baseCurrency: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoIndication(rememberInteractionSource()) {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = displayBudget.budget.name,
                style = UI.typo.b1.style(
                    color = UI.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = determineBudgetType(displayBudget.budget.parseCategoryIds().size),
                style = UI.typo.c.style(
                    color = Gray
                )
            )
        }

        AmountCurrencyB1(
            amount = displayBudget.budget.amount,
            currency = baseCurrency,
            amountFontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.width(32.dp))
    }

    Spacer(Modifier.height(12.dp))

    BudgetBattery(
        modifier = Modifier.padding(horizontal = 16.dp),
        currency = baseCurrency,
        expenses = displayBudget.spentAmount,
        budget = displayBudget.budget.amount,
        backgroundNotFilled = UI.colors.medium
    ) {
        onClick()
    }
}

@Composable
private fun NoBudgetsEmptyState(
    emptyStateTitle: String,
    emptyStateText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        IvyIcon(
            icon = R.drawable.ic_budget_xl,
            tint = Gray
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = emptyStateTitle,
            style = UI.typo.b1.style(
                color = Gray,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = emptyStateText,
            style = UI.typo.b2.style(
                color = Gray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )

        Spacer(Modifier.height(96.dp))
    }
}