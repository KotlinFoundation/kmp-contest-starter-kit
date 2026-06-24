plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kotlin.multiplatform.library).apply(false)
    alias(libs.plugins.google.services).apply(false)
    alias(libs.plugins.firebase.crashlytics).apply(false)
    alias(libs.plugins.buildConfig).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.room).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.spotless)
}

val ktlintVersion = libs.versions.ktlint.get()

// Rules disabled because they fight Compose conventions or are stylistic preferences,
// not correctness issues. `function-naming` collides with @Composable PascalCase and
// backtick test names; the comment / line-length rules just nag.
val ktlintRuleOverrides =
    mapOf(
        "ktlint_standard_function-naming" to "disabled",
        "ktlint_standard_value-parameter-comment" to "disabled",
        "ktlint_standard_max-line-length" to "disabled",
        "ktlint_standard_no-wildcard-imports" to "disabled",
        "ktlint_standard_property-naming" to "disabled",
    )

// Root project: only format Gradle KTS at the root.
spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(ktlintVersion).editorConfigOverride(ktlintRuleOverrides)
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            targetExclude("**/build/**", "**/generated/**")
            ktlint(ktlintVersion).editorConfigOverride(ktlintRuleOverrides)
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(ktlintVersion).editorConfigOverride(ktlintRuleOverrides)
        }
    }

    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin> {
        extensions
            .findByType<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension>()
            ?.jvmToolchain(17)
    }
}
