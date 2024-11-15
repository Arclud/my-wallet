package kg.ivy.data.backup

import androidx.annotation.Keep
import kg.ivy.data.db.entity.AccountEntity
import kg.ivy.data.db.entity.BudgetEntity
import kg.ivy.data.db.entity.CategoryEntity
import kg.ivy.data.db.entity.LoanEntity
import kg.ivy.data.db.entity.LoanRecordEntity
import kg.ivy.data.db.entity.PlannedPaymentRuleEntity
import kg.ivy.data.db.entity.SettingsEntity
import kg.ivy.data.db.entity.TagAssociationEntity
import kg.ivy.data.db.entity.TagEntity
import kg.ivy.data.db.entity.TransactionEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@Suppress("DataClassDefaultValues")
data class IvyWalletCompleteData(
    @SerialName("accounts")
    val accounts: List<AccountEntity> = emptyList(),
    @SerialName("budgets")
    val budgets: List<BudgetEntity> = emptyList(),
    @SerialName("categories")
    val categories: List<CategoryEntity> = emptyList(),
    @SerialName("loanRecords")
    val loanRecords: List<LoanRecordEntity> = emptyList(),
    @SerialName("loans")
    val loans: List<LoanEntity> = emptyList(),
    @SerialName("plannedPaymentRules")
    val plannedPaymentRules: List<PlannedPaymentRuleEntity> = emptyList(),
    @SerialName("settings")
    val settings: List<SettingsEntity> = emptyList(),
    @SerialName("transactions")
    val transactions: List<TransactionEntity> = emptyList(),
    @SerialName("sharedPrefs")
    val sharedPrefs: HashMap<String, String> = HashMap(),
    @SerialName("tags")
    val tags: List<TagEntity> = emptyList(),
    @SerialName("tagAssociations")
    val tagAssociations: List<TagAssociationEntity> = emptyList()
)
