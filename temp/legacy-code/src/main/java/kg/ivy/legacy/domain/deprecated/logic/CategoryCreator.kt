package kg.ivy.wallet.domain.deprecated.logic

import androidx.compose.ui.graphics.toArgb
import arrow.core.raise.either
import kg.ivy.data.model.Category
import kg.ivy.data.model.CategoryId
import kg.ivy.data.model.primitive.ColorInt
import kg.ivy.data.model.primitive.IconAsset
import kg.ivy.data.model.primitive.NotBlankTrimmedString
import kg.ivy.data.repository.CategoryRepository
import kg.ivy.legacy.utils.ioThread
import kg.ivy.wallet.domain.deprecated.logic.model.CreateCategoryData
import kg.ivy.wallet.domain.pure.util.nextOrderNum
import java.util.UUID
import javax.inject.Inject

class CategoryCreator @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend fun createCategory(
        data: CreateCategoryData,
        onRefreshUI: suspend (Category) -> Unit
    ) {
        val name = data.name
        if (name.isBlank()) return

        try {
            val newCategory = ioThread {
                val newCategory: Category? = either {
                    Category(
                        name = NotBlankTrimmedString.from(name.trim()).bind(),
                        color = ColorInt(data.color.toArgb()),
                        icon = data.icon?.let(IconAsset::from)?.getOrNull(),
                        orderNum = categoryRepository.findMaxOrderNum().nextOrderNum(),
                        id = CategoryId(UUID.randomUUID()),
                    )
                }.getOrNull()

                if (newCategory != null) {
                    categoryRepository.save(newCategory)
                }
                newCategory
            }

            newCategory?.let { onRefreshUI(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun editCategory(
        updatedCategory: Category,
        onRefreshUI: suspend (Category) -> Unit
    ) {
        if (updatedCategory.name.value.isBlank()) return

        try {
            ioThread {
                categoryRepository.save(updatedCategory)
            }

            onRefreshUI(updatedCategory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
