package kg.ivy.piechart

import androidx.compose.runtime.Immutable
import kg.ivy.data.model.Category

@Immutable
data class SelectedCategory(
    val category: Category // null - Unspecified
)
