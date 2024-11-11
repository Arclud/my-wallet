package kg.ivy.legacy.datamodel.temp

import kg.ivy.data.db.entity.PlannedPaymentRuleEntity
import kg.ivy.legacy.datamodel.PlannedPaymentRule

fun PlannedPaymentRuleEntity.toLegacyDomain(): PlannedPaymentRule = PlannedPaymentRule(
    startDate = startDate,
    intervalN = intervalN,
    intervalType = intervalType,
    oneTime = oneTime,
    type = type,
    accountId = accountId,
    amount = amount,
    categoryId = categoryId,
    title = title,
    description = description,
    isSynced = isSynced,
    isDeleted = isDeleted,
    id = id
)
