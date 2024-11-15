plugins {
    id("ivy.feature")
}

android {
    namespace = "kg.ivy.ui"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.domain)
}