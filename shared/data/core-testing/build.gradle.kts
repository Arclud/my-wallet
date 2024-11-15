plugins {
    id("ivy.feature")
    id("ivy.room")
}

android {
    namespace = "kg.ivy.data.testing"
}

dependencies {
    implementation(projects.shared.data.core)
}
