package kg.ivy.data.model.primitive

import kg.ivy.data.model.sync.UniqueId
import java.util.UUID

@JvmInline
value class AssociationId(override val value: UUID) : UniqueId