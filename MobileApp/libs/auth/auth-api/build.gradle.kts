plugins {
    id("configure-kmp-library-module")
}

kotlin {
    android {
        namespace = "com.kotlinfoundation.kmpstarterkit.auth.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
