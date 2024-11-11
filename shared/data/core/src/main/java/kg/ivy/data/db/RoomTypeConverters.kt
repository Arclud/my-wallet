package kg.ivy.data.db

import androidx.room.TypeConverter
import kg.ivy.base.legacy.Theme
import kg.ivy.base.legacy.epochMilliToDateTime
import kg.ivy.base.legacy.toEpochMilli
import kg.ivy.base.model.LoanRecordType
import kg.ivy.base.model.TransactionType
import kg.ivy.data.model.IntervalType
import kg.ivy.data.model.LoanType
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@SuppressWarnings("unused")
class RoomTypeConverters {
    @TypeConverter
    fun saveDate(localDateTime: LocalDateTime?): Long? = localDateTime?.toEpochMilli()

    @TypeConverter
    fun parseDate(timestampMillis: Long?): LocalDateTime? = timestampMillis?.epochMilliToDateTime()

    @TypeConverter
    fun saveUUID(id: UUID?) = id?.toString()

    @TypeConverter
    fun parseUUID(id: String?) = id?.let { UUID.fromString(id) }

    @TypeConverter
    fun saveTheme(value: Theme?) = value?.name

    @TypeConverter
    fun parseTheme(value: String?) = value?.let { Theme.valueOf(it) }

    @TypeConverter
    fun saveTransactionType(value: TransactionType?) = value?.name

    @TypeConverter
    fun parseTransactionType(value: String?) = value?.let { TransactionType.valueOf(it) }

    @TypeConverter
    fun saveRecurringIntervalType(value: IntervalType?) = value?.name

    @TypeConverter
    fun parseRecurringIntervalType(value: String?) =
        value?.let { IntervalType.valueOf(it) }

    @TypeConverter
    fun saveLoanType(value: LoanType?) = value?.name

    @TypeConverter
    fun parseLoanType(value: String?) = value?.let { LoanType.valueOf(it) }

    @TypeConverter
    fun saveInstant(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun parseInstant(value: Long): Instant = Instant.ofEpochMilli(value)

    @TypeConverter
    fun saveLoanRecordType(value: LoanRecordType?): String? = value?.name

    @TypeConverter
    fun parseLoanRecordType(value: String?): LoanRecordType? =
        value?.let { LoanRecordType.valueOf(it) }
}
