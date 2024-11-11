package kg.ivy.design.api

import kg.ivy.base.legacy.Theme
import kg.ivy.design.IvyContext
import kg.ivy.design.l0_system.IvyColors
import kg.ivy.design.l0_system.IvyShapes
import kg.ivy.design.l0_system.IvyTypography

@Deprecated("Old design system. Use `:ivy-design` and Material3")
interface IvyDesign {
    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun context(): IvyContext

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun typography(): IvyTypography

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun colors(theme: Theme, isDarkModeEnabled: Boolean): IvyColors

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun shapes(): IvyShapes
}
