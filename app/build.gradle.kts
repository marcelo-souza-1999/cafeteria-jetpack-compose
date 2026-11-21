import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.google.gms.services)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "com.targaryen.cafeteria.app"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.targaryen.cafeteria.app"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val secretsFile = rootProject.file("secrets.properties")
        val secrets = Properties()
        if (secretsFile.exists()) {
            secretsFile.inputStream().use { secrets.load(it) }
        }
        val mpPublicKey = secrets.getProperty("MERCADO_PAGO_PUBLIC_KEY", "")
        val mpAccessToken = secrets.getProperty("MERCADO_PAGO_ACCESS_TOKEN", "")

        buildConfigField("String", "MERCADO_PAGO_PUBLIC_KEY", "\"$mpPublicKey\"")
        buildConfigField("String", "MERCADO_PAGO_ACCESS_TOKEN", "\"$mpAccessToken\"")
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            manifestPlaceholders["appName"] = "Cafeteria Targaryen Dev"
            buildConfigField("String", "ENVIRONMENT_NAME", "\"dev\"")
            buildConfigField("boolean", "ENABLE_STRICT_PINNING", "false")
            buildConfigField("boolean", "ENABLE_TELEMETRY", "true")
        }
        create("prod") {
            dimension = "environment"
            manifestPlaceholders["appName"] = "@string/app_name"
            buildConfigField("String", "ENVIRONMENT_NAME", "\"prod\"")
            buildConfigField("boolean", "ENABLE_STRICT_PINNING", "true")
            buildConfigField("boolean", "ENABLE_TELEMETRY", "true")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        val javaVersion = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        disable += "Instantiatable"
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:catalog"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:checkout"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.browser)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.alert.dialog)

    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.compose.icons)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.coil)
    implementation(libs.androidx.appfunctions)
    ksp(libs.androidx.appfunctions.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.firebase.ai)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.debug)

    testImplementation(libs.bundles.koin.test)
    testImplementation(libs.junit)
    testImplementation(libs.mockk.io)
    testImplementation(libs.turbine.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.bundles.test.core)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.bundles.koin.test)
    androidTestImplementation(libs.bundles.test.core)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register("detektAll") {
    dependsOn(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>())
}

@CacheableTask
abstract class VerifyHardeningTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val proguardFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val networkSecurityFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val manifestFile: RegularFileProperty

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val proguardText = proguardFile.get().asFile.readText()
        val networkText = networkSecurityFile.get().asFile.readText()
        val manifestText = manifestFile.get().asFile.readText()

        val checks =
            listOf(
                "r8_log_stripping" to proguardText.contains("-assumenosideeffects class android.util.Log"),
                "r8_stacktrace_attributes" to
                    (proguardText.contains("SourceFile") && proguardText.contains("LineNumberTable")),
                "cleartext_disabled" to networkText.contains("cleartextTrafficPermitted=\"false\""),
                "certificate_pinning" to networkText.contains("<pin-set"),
                "manifest_network_config" to
                    manifestText.contains("android:networkSecurityConfig=\"@xml/network_security_config\""),
                "manifest_backup_disabled" to manifestText.contains("android:allowBackup=\"false\""),
            )

        val failed = checks.filterNot { entry -> entry.second }.map { entry -> entry.first }
        val report =
            buildString {
                checks.forEach { entry ->
                    appendLine("${entry.first}=${if (entry.second) "PASS" else "FAIL"}")
                }
            }
        val outFile = reportFile.get().asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(report)

        if (failed.isNotEmpty()) {
            throw GradleException("Hardening verification failed: ${failed.joinToString()}")
        }
    }
}

tasks.register<VerifyHardeningTask>("verifyReleaseHardening") {
    group = "verification"
    description = "Audita regras de R8, Network Security Pinning e flags de Hardening no Manifesto."
    proguardFile.set(layout.projectDirectory.file("proguard-rules.pro"))
    networkSecurityFile.set(layout.projectDirectory.file("src/main/res/xml/network_security_config.xml"))
    manifestFile.set(layout.projectDirectory.file("src/main/AndroidManifest.xml"))
    reportFile.set(layout.buildDirectory.file("reports/hardening/hardening-verification.txt"))
}

koinCompiler {
    compileSafety = true
    strictSafety = true
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*ComposableSingletons*",
                    "*_Factory*",
                    "*MapperImpl*",
                    "*Screen*",
                    "*ScreenKt*",
                    "*Section*",
                    "*SectionKt*",
                    "*Dialog*",
                    "*DialogKt*",
                    "*BottomSheet*",
                    "*BottomSheetKt*",
                    "*Activity*",
                    "*ActivityKt*",
                    "*Application*",
                    "*ApplicationKt*",
                    "*Preview*",
                    "*PreviewKt*",
                    "*Theme*",
                    "*ThemeKt*",
                    "*Color*",
                    "*TypeKt*",
                    "*Dimens*",
                    "*Dao_Impl*",
                    "*Database_Impl*",
                    "*ModuleKt*",
                    "*AppFunctionService*",
                    "*_AppFunction*",
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
    }
}
