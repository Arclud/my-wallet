package kg.ivy.domain.model

import kg.ivy.data.model.testing.shouldBeApprox
import io.kotest.matchers.shouldBe

infix fun StatSummary.shouldBeApprox(other: StatSummary) {
    trnCount shouldBe other.trnCount
    values.keys shouldBe other.values.keys
    values.keys.forEach { key ->
        values[key]!!.value shouldBeApprox other.values[key]!!.value
    }
}