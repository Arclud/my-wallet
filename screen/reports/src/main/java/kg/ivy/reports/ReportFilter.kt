package kg.ivy.reports

import kg.ivy.base.model.TransactionType
import kg.ivy.data.model.Category
import kg.ivy.data.model.TagId
import kg.ivy.legacy.data.model.TimePeriod
import kg.ivy.legacy.datamodel.Account
import java.util.UUID

data class ReportFilter(
    val id: UUID = UUID.randomUUID(),
    val trnTypes: List<TransactionType>,
    val period: TimePeriod?,
    val accounts: List<Account>,
    val categories: List<Category>,
    val currency: String,
    val minAmount: Double?,
    val maxAmount: Double?,
    val includeKeywords: List<String>,
    val excludeKeywords: List<String>,
    val includedTags: List<TagId>,
    val excludedTags: List<TagId>,

) {
    companion object {
        fun emptyFilter(
            baseCurrency: String
        ) = ReportFilter(
            trnTypes = emptyList(),
            period = null,
            accounts = emptyList(),
            categories = emptyList(),
            currency = baseCurrency,
            includeKeywords = emptyList(),
            excludeKeywords = emptyList(),
            minAmount = null,
            maxAmount = null,
            includedTags = emptyList(),
            excludedTags = emptyList()
        )
    }

    fun validate(): Boolean {
        if (trnTypes.isEmpty()) return false

        if (period == null) return false

        if (accounts.isEmpty()) return false

        if (categories.isEmpty()) return false

        if (minAmount != null && maxAmount != null) {
            if (minAmount > maxAmount) return false
            if (maxAmount < minAmount) return false
        }

        return true
    }
}
