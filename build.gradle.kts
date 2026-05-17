buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hotswan.compiler) apply false
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        reports {
            html.required.set(true)
            xml.required.set(false)
            txt.required.set(false)
            sarif.required.set(true)
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.addAll(
                "-Xexplicit-backing-fields",
                "-opt-in=kotlin.ExperimentalStdlibApi"
            )
        }
    }
}

dependencies {
    add("kover", project(":app"))
    add("kover", project(":core:database"))
    add("kover", project(":core:designsystem"))
    add("kover", project(":core:network"))
    add("kover", project(":feature:auth"))
    add("kover", project(":feature:cart"))
    add("kover", project(":feature:catalog"))
    add("kover", project(":feature:chat"))
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*ComposableSingletons*",
                    "*_Factory*",
                    "*MapperImpl*",
                    "*BuildConfig*",
                    "*Resource*",
                    "*.R",
                    "*.R$*",
                    "*Gencom*",
                    "*KoinMeta*",
                    "org.koin.ksp.generated.*",
                    "com.targaryen.cafeteria.feature.auth.presentation.components.ComposableSingletons*",
                    "com.targaryen.cafeteria.feature.auth.presentation.login.ComposableSingletons*",
                    "com.targaryen.cafeteria.feature.auth.presentation.splash.ComposableSingletons*"
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
    }
}
