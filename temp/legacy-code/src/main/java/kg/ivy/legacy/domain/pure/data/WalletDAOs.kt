package kg.ivy.wallet.domain.pure.data

import kg.ivy.data.db.dao.read.AccountDao
import kg.ivy.data.db.dao.read.ExchangeRatesDao
import kg.ivy.data.db.dao.read.TransactionDao
import javax.inject.Inject

data class WalletDAOs @Inject constructor(
    val accountDao: AccountDao,
    val transactionDao: TransactionDao,
    val exchangeRatesDao: ExchangeRatesDao
)
