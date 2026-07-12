plugins {
    id("configure-kmp-library-module")
}

kotlin {
    android {
        namespace = "com.kotlinfoundation.koko.auth.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
