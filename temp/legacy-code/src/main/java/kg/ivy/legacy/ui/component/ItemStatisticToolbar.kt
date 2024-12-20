package kg.ivy.legacy.ui.component

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kg.ivy.base.legacy.stringRes
import kg.ivy.navigation.navigation
import kg.ivy.ui.R
import kg.ivy.wallet.ui.theme.Transparent
import kg.ivy.wallet.ui.theme.components.CircleButton
import kg.ivy.wallet.ui.theme.components.DeleteButton
import kg.ivy.wallet.ui.theme.components.IvyOutlinedButton

@SuppressLint("ComposeModifierMissing")
@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun ItemStatisticToolbar(
    contrastColor: Color,
    onEdit: () -> Unit,
    showEditButton: Boolean = true,
    showDeleteButton: Boolean = true,
    onDelete: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        val nav = navigation()
        CircleButton(
            modifier = Modifier.testTag("toolbar_close"),
            icon = R.drawable.ic_dismiss,
            borderColor = contrastColor,
            tint = contrastColor,
            backgroundColor = Transparent
        ) {
            nav.back()
        }

        Spacer(Modifier.weight(1f))

        if (showEditButton) {
            IvyOutlinedButton(
                iconStart = R.drawable.ic_edit,
                text = stringRes(R.string.edit),
                borderColor = contrastColor,
                iconTint = contrastColor,
                textColor = contrastColor,
                solidBackground = false
            ) {
                onEdit()
            }
        }

        Spacer(Modifier.width(16.dp))

        if (showDeleteButton) {
            DeleteButton {
                onDelete()
            }
        }

        Spacer(Modifier.width(24.dp))
    }
}
