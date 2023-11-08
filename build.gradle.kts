// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    id("org.jetbrains.kotlinx.kover") version "0.7.4"
}

val androidModules = listOf("fu-text-edit", "fu-edit-control")
subprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")

    dependencies {
        if (name == "app") {
            val action = { it: String ->
                kover(project(":$it"))
                Unit
            }
            androidModules.forEach(action)
        }
    }
    koverReport {
        defaults {
            mergeWith("release")
        }
        // filters for all report types of all build variants
        filters {
            excludes {
                classes(
                    "*Fragment",
                    "*Fragment\$*",
                    "*Activity",
                    "*Activity\$*",
                    "*.databinding.*",
                    "*.BuildConfig"
                )
            }
        }
    }
}