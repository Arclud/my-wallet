package kg.ivy.data.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import kg.ivy.data.db.dao.read.AccountDao
import kg.ivy.data.db.dao.read.BudgetDao
import kg.ivy.data.db.dao.read.CategoryDao
import kg.ivy.data.db.dao.read.ExchangeRatesDao
import kg.ivy.data.db.dao.read.LoanDao
import kg.ivy.data.db.dao.read.LoanRecordDao
import kg.ivy.data.db.dao.read.PlannedPaymentRuleDao
import kg.ivy.data.db.dao.read.SettingsDao
import kg.ivy.data.db.dao.read.TagDao
import kg.ivy.data.db.dao.read.TagAssociationDao
import kg.ivy.data.db.dao.read.TransactionDao
import kg.ivy.data.db.dao.read.UserDao
import kg.ivy.data.db.dao.write.WriteAccountDao
import kg.ivy.data.db.dao.write.WriteBudgetDao
import kg.ivy.data.db.dao.write.WriteCategoryDao
import kg.ivy.data.db.dao.write.WriteExchangeRatesDao
import kg.ivy.data.db.dao.write.WriteLoanDao
import kg.ivy.data.db.dao.write.WriteLoanRecordDao
import kg.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import kg.ivy.data.db.dao.write.WriteSettingsDao
import kg.ivy.data.db.dao.write.WriteTagDao
import kg.ivy.data.db.dao.write.WriteTagAssociationDao
import kg.ivy.data.db.dao.write.WriteTransactionDao
import kg.ivy.data.db.entity.AccountEntity
import kg.ivy.data.db.entity.BudgetEntity
import kg.ivy.data.db.entity.CategoryEntity
import kg.ivy.data.db.entity.ExchangeRateEntity
import kg.ivy.data.db.entity.LoanEntity
import kg.ivy.data.db.entity.LoanRecordEntity
import kg.ivy.data.db.entity.PlannedPaymentRuleEntity
import kg.ivy.data.db.entity.SettingsEntity
import kg.ivy.data.db.entity.TagEntity
import kg.ivy.data.db.entity.TagAssociationEntity
import kg.ivy.data.db.entity.TransactionEntity
import kg.ivy.data.db.entity.UserEntity

@Database(
    entities = [
        AccountEntity::class, TransactionEntity::class, CategoryEntity::class,
        SettingsEntity::class, PlannedPaymentRuleEntity::class,
        UserEntity::class, ExchangeRateEntity::class, BudgetEntity::class,
        LoanEntity::class, LoanRecordEntity::class, TagEntity::class, TagAssociationEntity::class
    ],
   /* autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2,
            spec = IvyRoomDatabase.DeleteSEMigration::class
        )
    ],*/
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters::class)
abstract class IvyRoomDatabase : RoomDatabase() {
    abstract val accountDao: AccountDao
    abstract val transactionDao: TransactionDao
    abstract val categoryDao: CategoryDao
    abstract val budgetDao: BudgetDao
    abstract val plannedPaymentRuleDao: PlannedPaymentRuleDao
    abstract val settingsDao: SettingsDao
    abstract val userDao: UserDao
    abstract val exchangeRatesDao: ExchangeRatesDao
    abstract val loanDao: LoanDao
    abstract val loanRecordDao: LoanRecordDao
    abstract val tagDao: TagDao
    abstract val tagAssociationDao: TagAssociationDao

    abstract val writeAccountDao: WriteAccountDao
    abstract val writeTransactionDao: WriteTransactionDao
    abstract val writeCategoryDao: WriteCategoryDao
    abstract val writeBudgetDao: WriteBudgetDao
    abstract val writePlannedPaymentRuleDao: WritePlannedPaymentRuleDao
    abstract val writeSettingsDao: WriteSettingsDao
    abstract val writeExchangeRatesDao: WriteExchangeRatesDao
    abstract val writeLoanDao: WriteLoanDao
    abstract val writeLoanRecordDao: WriteLoanRecordDao
    abstract val writeTagDao: WriteTagDao
    abstract val writeTagAssociationDao: WriteTagAssociationDao

    companion object {
        const val DB_NAME = "ivywallet.db"

        fun migrations() = arrayOf<Migration>()

        @Suppress("SpreadOperator")
        fun create(applicationContext: Context): IvyRoomDatabase {
            return Room
                .databaseBuilder(
                    applicationContext,
                    IvyRoomDatabase::class.java,
                    DB_NAME
                )
                .addMigrations(*migrations())
                .build()
        }
    }

    @DeleteColumn(tableName = "accounts", columnName = "seAccountId")
    @DeleteColumn(tableName = "transactions", columnName = "seTransactionId")
    @DeleteColumn(tableName = "transactions", columnName = "seAutoCategoryId")
    @DeleteColumn(tableName = "categories", columnName = "seCategoryName")
    class DeleteSEMigration : AutoMigrationSpec
}
