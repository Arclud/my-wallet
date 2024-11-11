package kg.ivy.data.model.testing

import kg.ivy.data.model.AccountId
import kg.ivy.data.model.CategoryId
import kg.ivy.data.model.TransactionId
import java.util.UUID

object ModelFixtures {
    val AccountId = AccountId(UUID.randomUUID())
    val CategoryId = CategoryId(UUID.randomUUID())
    val TransactionId = TransactionId(UUID.randomUUID())
}
