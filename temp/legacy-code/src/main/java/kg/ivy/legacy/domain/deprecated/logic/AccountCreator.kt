package kg.ivy.legacy.domain.deprecated.logic

import androidx.compose.ui.graphics.toArgb
import arrow.core.raise.either
import kg.ivy.data.db.dao.read.AccountDao
import kg.ivy.data.model.Account
import kg.ivy.data.model.AccountId
import kg.ivy.data.model.primitive.AssetCode
import kg.ivy.data.model.primitive.ColorInt
import kg.ivy.data.model.primitive.IconAsset
import kg.ivy.data.model.primitive.NotBlankTrimmedString
import kg.ivy.data.repository.AccountRepository
import kg.ivy.data.repository.CurrencyRepository
import kg.ivy.legacy.utils.ioThread
import kg.ivy.wallet.domain.deprecated.logic.WalletAccountLogic
import kg.ivy.wallet.domain.deprecated.logic.model.CreateAccountData
import kg.ivy.wallet.domain.pure.util.nextOrderNum
import java.util.UUID
import javax.inject.Inject
import kg.ivy.legacy.datamodel.Account as LegacyAccount

class AccountCreator @Inject constructor(
    private val accountLogic: WalletAccountLogic,
    private val accountDao: AccountDao,
    private val accountRepository: AccountRepository,
    private val currencyRepository: CurrencyRepository,
) {

    suspend fun createAccount(
        data: CreateAccountData,
        onRefreshUI: suspend () -> Unit
    ) {
        ioThread {
            val account = either {
                Account(
                    id = AccountId(value = UUID.randomUUID()),
                    name = NotBlankTrimmedString.from(data.name).bind(),
                    asset = AssetCode.from(data.currency).bind(),
                    color = ColorInt(data.color.toArgb()),
                    icon = data.icon?.let(IconAsset::from)?.getOrNull(),
                    includeInBalance = data.includeBalance,
                    orderNum = accountDao.findMaxOrderNum().nextOrderNum(),
                )
            }.getOrNull() ?: return@ioThread
            accountRepository.save(account)

            val legacyAccount = LegacyAccount(
                name = data.name,
                currency = data.currency,
                color = data.color.toArgb(),
                icon = data.icon,
                includeInBalance = data.includeBalance,
                orderNum = accountDao.findMaxOrderNum().nextOrderNum(),
                isSynced = false,
                id = account.id.value
            )
            accountLogic.adjustBalance(
                account = legacyAccount,
                actualBalance = 0.0,
                newBalance = data.balance
            )
        }

        onRefreshUI()
    }

    suspend fun editAccount(
        legacyAccount: LegacyAccount,
        newBalance: Double,
        onRefreshUI: suspend () -> Unit
    ) {
        val updatedLegacyAccount = legacyAccount.copy(
            isSynced = false
        )
        ioThread {
            val account = legacyAccount.toDomainAccount(currencyRepository).getOrNull()
                ?: return@ioThread
            accountRepository.save(account)

            accountLogic.adjustBalance(
                account = updatedLegacyAccount,
                actualBalance = accountLogic.calculateAccountBalance(updatedLegacyAccount),
                newBalance = newBalance
            )
        }

        onRefreshUI()
    }
}
