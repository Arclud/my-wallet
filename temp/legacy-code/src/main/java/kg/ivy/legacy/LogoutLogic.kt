package kg.ivy.legacy

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kg.ivy.base.legacy.SharedPrefs
import kg.ivy.data.DataObserver
import kg.ivy.data.DataWriteEvent
import kg.ivy.data.db.dao.read.UserDao
import kg.ivy.data.db.dao.write.WriteBudgetDao
import kg.ivy.data.db.dao.write.WriteLoanDao
import kg.ivy.data.db.dao.write.WriteLoanRecordDao
import kg.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import kg.ivy.data.db.dao.write.WriteSettingsDao
import kg.ivy.data.repository.AccountRepository
import kg.ivy.data.repository.CategoryRepository
import kg.ivy.data.repository.ExchangeRatesRepository
import kg.ivy.data.repository.TagRepository
import kg.ivy.data.repository.TransactionRepository
import kg.ivy.legacy.utils.ioThread
import kg.ivy.navigation.MainScreen
import kg.ivy.navigation.Navigation
import kg.ivy.navigation.OnboardingScreen
import javax.inject.Inject

@Deprecated("Migrate to an UseCase in the domain layer.")
class LogoutLogic @Inject constructor(
    private val sharedPrefs: SharedPrefs,
    private val navigation: Navigation,
    private val dataObserver: DataObserver,
    private val dataStore: DataStore<Preferences>,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val userDao: UserDao,
    private val writeSettingsDao: WriteSettingsDao,
    private val writePlannedPaymentRuleDao: WritePlannedPaymentRuleDao,
    private val writeBudgetDao: WriteBudgetDao,
    private val writeLoanDao: WriteLoanDao,
    private val writeLoanRecordDao: WriteLoanRecordDao,
    private val exchangeRatesRepository: ExchangeRatesRepository
) {
    suspend fun logout() {
        ioThread {
            deleteAllData()
            dataStore.edit {
                it.clear()
            }
            sharedPrefs.removeAll()
        }

        dataObserver.post(DataWriteEvent.AllDataChange)
        navigation.resetBackStack()
        navigation.navigateTo(OnboardingScreen)
    }

    private suspend fun deleteAllData() {
        accountRepository.deleteAll()
        transactionRepository.deleteAll()
        categoryRepository.deleteAll()
        tagRepository.deleteAll()
        writeSettingsDao.deleteAll()
        writePlannedPaymentRuleDao.deleteAll()
        userDao.deleteAll()
        writeBudgetDao.deleteAll()
        writeLoanDao.deleteAll()
        writeLoanRecordDao.deleteAll()
        exchangeRatesRepository.deleteAll()
    }

    suspend fun cloudLogout() {
        navigation.navigateTo(MainScreen)
    }
}
