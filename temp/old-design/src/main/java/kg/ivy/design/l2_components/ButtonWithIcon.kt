package kg.ivy.design.l2_components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kg.ivy.design.R
import kg.ivy.design.l0_system.Transparent
import kg.ivy.design.l0_system.UI
import kg.ivy.design.l0_system.White
import kg.ivy.design.l0_system.style
import kg.ivy.design.l1_buildingBlocks.IvyIcon
import kg.ivy.design.l1_buildingBlocks.SpacerHor
import kg.ivy.design.l1_buildingBlocks.data.Background
import kg.ivy.design.l1_buildingBlocks.data.background
import kg.ivy.design.l1_buildingBlocks.data.clipBackground
import kg.ivy.design.utils.IvyComponentPreview
import kg.ivy.design.utils.padding

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun Button(
    modifier: Modifier = Modifier,
    text: String,
    mode: Mode = Mode.WRAP_CONTENT,
    background: Background = Background.Solid(
        color = UI.colors.primary,
        shape = UI.shapes.rFull,
        padding = padding(
            horizontal = 24.dp,
            vertical = 12.dp
        )
    ),
    textStyle: TextStyle = UI.typo.b1.style(
        color = White,
        textAlign = TextAlign.Center
    ),
    @DrawableRes iconLeft: Int? = null,
    @DrawableRes iconRight: Int? = null,
    iconTint: Color = White,
    iconPadding: Dp = 12.dp,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clipBackground(background)
            .clickable(
                onClick = onClick
            )
            .background(background),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val icon = iconLeft ?: iconRight ?: error("If you don't add icon, use Button()")

        if (mode != Mode.WRAP_CONTENT || iconLeft != null) {
            IvyIcon(
                icon = icon,
                tint = if (iconLeft != null) iconTint else Transparent
            )

            SpacerHor(width = iconPadding)
        }

        Text(
            text = text,
            style = textStyle
        )

        if (mode != Mode.WRAP_CONTENT || iconRight != null) {
            SpacerHor(width = iconPadding)

            IvyIcon(
                icon = icon,
                tint = if (iconRight != null) iconTint else Transparent
            )
        }
    }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
enum class Mode {
    WRAP_CONTENT, FILL_MAX_WIDTH
}

@Preview
@Composable
private fun Preview_IconLeft_Wrap() {
    IvyComponentPreview {
        Button(
            text = "Button",
            mode = Mode.WRAP_CONTENT,
            iconLeft = R.drawable.ic_android_black_24dp
        ) {
        }
    }
}

@Preview
@Composable
private fun Preview_IconRight_Wrap() {
    IvyComponentPreview {
        Button(
            text = "Button",
            mode = Mode.WRAP_CONTENT,
            iconRight = R.drawable.ic_android_black_24dp
        ) {
        }
    }
}

@Preview
@Composable
private fun Preview_IconLeft_FillMax() {
    IvyComponentPreview {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            text = "Button",
            mode = Mode.FILL_MAX_WIDTH,
            iconLeft = R.drawable.ic_android_black_24dp
        ) {
        }
    }
}

@Preview
@Composable
private fun Preview_IconRight_FillMax() {
    IvyComponentPreview {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            text = "Button",
            mode = Mode.FILL_MAX_WIDTH,
            iconRight = R.drawable.ic_android_black_24dp
        ) {
        }
    }
}
