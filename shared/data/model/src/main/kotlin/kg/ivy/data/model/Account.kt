package kg.ivy.data.model

import kg.ivy.data.model.primitive.AssetCode
import kg.ivy.data.model.primitive.ColorInt
import kg.ivy.data.model.primitive.IconAsset
import kg.ivy.data.model.primitive.NotBlankTrimmedString
import kg.ivy.data.model.sync.Identifiable
import kg.ivy.data.model.sync.UniqueId
import java.util.UUID

@JvmInline
value class AccountId(override val value: UUID) : UniqueId

data class Account(
    override val id: AccountId,
    val name: NotBlankTrimmedString,
    val asset: AssetCode,
    val color: ColorInt,
    val icon: IconAsset?,
    val includeInBalance: Boolean,
    override val orderNum: Double,
) : Identifiable<AccountId>, Reorderable
