plugins {
    id("ivy.feature")
}

android {
    namespace = "kg.ivy.navigation"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.domain)
    implementation(projects.shared.ui.core)
}
