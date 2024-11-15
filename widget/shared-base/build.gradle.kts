plugins {
    id("ivy.widget")
}

android {
    namespace = "kg.ivy.widget"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.domain)
}
