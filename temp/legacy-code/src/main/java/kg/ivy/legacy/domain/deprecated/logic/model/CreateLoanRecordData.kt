package kg.ivy.wallet.domain.deprecated.logic.model

import kg.ivy.base.model.LoanRecordType
import kg.ivy.legacy.datamodel.Account
import java.time.Instant

data class CreateLoanRecordData(
    val note: String?,
    val amount: Double,
    val dateTime: Instant,
    val interest: Boolean = false,
    val account: Account? = null,
    val createLoanRecordTransaction: Boolean = false,
    val convertedAmount: Double? = null,
    val loanRecordType: LoanRecordType
)
