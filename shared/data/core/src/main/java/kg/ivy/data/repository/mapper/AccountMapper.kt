package kg.ivy.data.repository.mapper

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import kg.ivy.data.db.entity.AccountEntity
import kg.ivy.data.model.Account
import kg.ivy.data.model.AccountId
import kg.ivy.data.model.primitive.AssetCode
import kg.ivy.data.model.primitive.ColorInt
import kg.ivy.data.model.primitive.IconAsset
import kg.ivy.data.model.primitive.NotBlankTrimmedString
import kg.ivy.data.repository.CurrencyRepository
import javax.inject.Inject

class AccountMapper @Inject constructor(
    private val currencyRepository: CurrencyRepository
) {
    suspend fun AccountEntity.toDomain(): Either<String, Account> = either {
        ensure(!isDeleted) { "Account is deleted" }

        Account(
            id = AccountId(id),
            name = NotBlankTrimmedString.from(name).bind(),
            asset = currency?.let(AssetCode::from)?.getOrNull()
                ?: currencyRepository.getBaseCurrency(),
            color = ColorInt(color),
            icon = icon?.let(IconAsset::from)?.getOrNull(),
            includeInBalance = includeInBalance,
            orderNum = orderNum,
        )
    }

    fun Account.toEntity(): AccountEntity {
        return AccountEntity(
            name = name.value,
            currency = asset.code,
            color = color.value,
            icon = icon?.id,
            orderNum = orderNum,
            includeInBalance = includeInBalance,
            id = id.value,
            isSynced = true, // TODO: Delete this
        )
    }
}
