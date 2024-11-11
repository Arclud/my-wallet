package kg.ivy.domain.model

import kg.ivy.data.model.primitive.AssetCode
import kg.ivy.data.model.primitive.NonNegativeInt
import kg.ivy.data.model.primitive.PositiveDouble

data class StatSummary(
    val trnCount: NonNegativeInt,
    val values: Map<AssetCode, PositiveDouble>,
) {
    companion object {
        val Zero = StatSummary(
            values = emptyMap(),
            trnCount = NonNegativeInt.Zero
        )
    }
}