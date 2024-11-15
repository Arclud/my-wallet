package kg.ivy.legacy.datamodel.temp

import kg.ivy.data.db.entity.CategoryEntity
import kg.ivy.legacy.datamodel.Category

fun CategoryEntity.toLegacyDomain(): Category = Category(
    name = name,
    color = color,
    icon = icon,
    orderNum = orderNum,
    isSynced = isSynced,
    isDeleted = isDeleted,
    id = id
)
