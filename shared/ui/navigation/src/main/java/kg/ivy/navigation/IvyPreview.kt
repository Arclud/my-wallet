package kg.ivy.navigation

import androidx.compose.runtime.Composable
import kg.ivy.design.system.IvyMaterial3Theme

@Composable
fun IvyPreview(
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    NavigationRoot(navigation = Navigation()) {
        IvyMaterial3Theme(dark = dark, isTrueBlack = false, content = content)
    }
}
