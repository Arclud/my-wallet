package kg.ivy.data.repository

import kg.ivy.base.threading.DispatchersProvider
import kg.ivy.data.DataWriteEvent
import kg.ivy.data.db.dao.read.AccountDao
import kg.ivy.data.db.dao.write.WriteAccountDao
import kg.ivy.data.model.Account
import kg.ivy.data.model.AccountId
import kg.ivy.data.repository.mapper.AccountMapper
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val mapper: AccountMapper,
    private val accountDao: AccountDao,
    private val writeAccountDao: WriteAccountDao,
    private val dispatchersProvider: DispatchersProvider,
    memoFactory: RepositoryMemoFactory,
) {
    private val memo = memoFactory.createMemo(
        getDataWriteSaveEvent = DataWriteEvent::SaveAccounts,
        getDateWriteDeleteEvent = DataWriteEvent::DeleteAccounts
    )

    suspend fun findById(id: AccountId): Account? = memo.findById(
        id = id,
        findByIdOperation = {
            accountDao.findById(id.value)?.let {
                with(mapper) { it.toDomain() }.getOrNull()
            }
        }
    )

    suspend fun findAll(): List<Account> = memo.findAll(
        findAllOperation = {
            accountDao.findAll().mapNotNull {
                with(mapper) { it.toDomain() }.getOrNull()
            }
        },
        sortMemo = { sortedBy(Account::orderNum) }
    )

    suspend fun findMaxOrderNum(): Double = if (memo.findAllMemoized) {
        memo.items.maxOfOrNull { (_, acc) -> acc.orderNum } ?: 0.0
    } else {
        withContext(dispatchersProvider.io) {
            accountDao.findMaxOrderNum() ?: 0.0
        }
    }

    suspend fun save(value: Account): Unit = memo.save(value) {
        writeAccountDao.save(
            with(mapper) { it.toEntity() }
        )
    }

    suspend fun saveMany(values: List<Account>): Unit = memo.saveMany(values) {
        writeAccountDao.saveMany(
            it.map { with(mapper) { it.toEntity() } }
        )
    }

    suspend fun deleteById(id: AccountId): Unit = memo.deleteById(id) {
        writeAccountDao.deleteById(id.value)
    }

    suspend fun deleteAll(): Unit = memo.deleteAll(
        deleteAllOperation = writeAccountDao::deleteAll
    )
}
