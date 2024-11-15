package kg.ivy.legacy.data.model

import androidx.compose.runtime.Immutable
import kg.ivy.base.time.TimeProvider
import kg.ivy.data.model.IntervalType
import kg.ivy.legacy.forDisplay
import kg.ivy.legacy.incrementDate
import java.time.Instant

@Suppress("DataClassFunctions")
@Immutable
data class LastNTimeRange(
    val periodN: Int,
    val periodType: IntervalType,
) {
    fun fromDate(
        timeProvider: TimeProvider
    ): Instant = periodType.incrementDate(
        date = timeProvider.utcNow(),
        intervalN = -periodN.toLong()
    )

    fun forDisplay(): String =
        "$periodN ${periodType.forDisplay(periodN)}"
}
