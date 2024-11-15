package kg.ivy.legacy

import kg.ivy.base.legacy.stringRes
import kg.ivy.data.model.LoanType
import kg.ivy.legacy.datamodel.Loan
import kg.ivy.ui.R

fun Loan.humanReadableType(): String {
    return if (type == LoanType.BORROW) {
        stringRes(R.string.borrowed_uppercase)
    } else {
        stringRes(R.string.lent_uppercase)
    }
}
