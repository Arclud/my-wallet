package kg.ivy.importdata.csvimport.flow

import androidx.compose.runtime.Composable
import kg.ivy.importdata.csvimport.flow.instructions.DefaultImportSteps
import kg.ivy.importdata.csvimport.flow.instructions.FinancistoSteps
import kg.ivy.importdata.csvimport.flow.instructions.FortuneCitySteps
import kg.ivy.importdata.csvimport.flow.instructions.IvyWalletSteps
import kg.ivy.importdata.csvimport.flow.instructions.KTWMoneyManagerSteps
import kg.ivy.importdata.csvimport.flow.instructions.MonefySteps
import kg.ivy.importdata.csvimport.flow.instructions.MoneyManagerPraseSteps
import kg.ivy.importdata.csvimport.flow.instructions.OneMoneySteps
import kg.ivy.importdata.csvimport.flow.instructions.SpendeeSteps
import kg.ivy.importdata.csvimport.flow.instructions.WalletByBudgetBakersSteps
import kg.ivy.legacy.domain.deprecated.logic.csv.model.ImportType

@Composable
fun ImportType.ImportSteps(
    onUploadClick: () -> Unit
) {
    when (this) {
        ImportType.IVY -> {
            IvyWalletSteps(
                onUploadClick = onUploadClick
            )
        }

        ImportType.MONEY_MANAGER -> {
            MoneyManagerPraseSteps(
                onUploadClick = onUploadClick
            )
        }

        ImportType.WALLET_BY_BUDGET_BAKERS -> {
            WalletByBudgetBakersSteps(
                onUploadClick = onUploadClick
            )
        }

        ImportType.SPENDEE -> SpendeeSteps(
            onUploadClick = onUploadClick
        )

        ImportType.MONEFY -> MonefySteps(
            onUploadClick = onUploadClick
        )

        ImportType.ONE_MONEY -> OneMoneySteps(
            onUploadClick = onUploadClick
        )

        ImportType.BLUE_COINS -> DefaultImportSteps(
            onUploadClick = onUploadClick
        )

        ImportType.KTW_MONEY_MANAGER -> KTWMoneyManagerSteps(
            onUploadClick = onUploadClick
        )

        ImportType.FORTUNE_CITY -> FortuneCitySteps(
            onUploadClick = onUploadClick
        )

        ImportType.FINANCISTO -> FinancistoSteps(
            onUploadClick = onUploadClick
        )
    }
}
