package kg.ivy.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration123to124_LoanIncludeDateTime : Migration(123, 124) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE loans ADD COLUMN dateTime INTEGER NOT NULL DEFAULT 0")
    }
}