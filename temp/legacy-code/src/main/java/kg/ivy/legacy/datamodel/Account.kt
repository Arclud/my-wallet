package kg.ivy.legacy.datamodel

import androidx.compose.runtime.Immutable
import arrow.core.Either
import arrow.core.raise.either
import kg.ivy.data.db.entity.AccountEntity
import kg.ivy.data.model.Account
import kg.ivy.data.model.AccountId
import kg.ivy.data.model.primitive.AssetCode
import kg.ivy.data.model.primitive.ColorInt
import kg.ivy.data.model.primitive.IconAsset
import kg.ivy.data.model.primitive.NotBlankTrimmedString
import kg.ivy.data.repository.CurrencyRepository
import java.util.UUID
import kg.ivy.data.model.Account as DomainAccount

@Deprecated("Legacy data model. Will be deleted")
@Immutable
data class Account(
    val name: String,
    val color: Int,
    val currency: String? = null,
    val icon: String? = null,
    val orderNum: Double = 0.0,
    val includeInBalance: Boolean = true,

    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    val id: UUID = UUID.randomUUID()
) {
    fun toEntity(): AccountEntity = AccountEntity(
        name = name,
        currency = currency,
        color = color,
        icon = icon,
        orderNum = orderNum,
        includeInBalance = includeInBalance,
        isSynced = isSynced,
        isDeleted = isDeleted,
        id = id
    )

    @Suppress("DataClassFunctions")
    suspend fun toDomainAccount(
        currencyRepository: CurrencyRepository
    ): Either<String, DomainAccount> {
        return either {
            Account(
                id = AccountId(id),
                name = NotBlankTrimmedString.from(name).bind(),
                asset = currency?.let(AssetCode::from)?.bind()
                    ?: currencyRepository.getBaseCurrency(),
                color = ColorInt(color),
                icon = icon?.let(IconAsset::from)?.getOrNull(),
                includeInBalance = includeInBalance,
                orderNum = orderNum,
            )
        }
    }
}
