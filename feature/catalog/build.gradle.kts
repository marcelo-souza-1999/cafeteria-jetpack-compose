import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hotswan.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "com.targaryen.cafeteria.feature_catalog"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    val localProperties =
        Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) {
                load(file.inputStream())
            }
        }
    val geminiKey = localProperties.getProperty("gemini.api.key") ?: System.getenv("GEMINI_API_KEY") ?: ""

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        }
        debug {
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        }
    }
    compileOptions {
        val javaVersion = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))

    implementation(libs.generativeai)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.bundles.compose.icons)

    implementation(libs.bundles.koin)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.coil)
    implementation(libs.androidx.appfunctions)
    implementation(libs.androidx.appfunctions.service)
    ksp(libs.androidx.appfunctions.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.compose.alert.dialog)

    testImplementation(libs.junit)
    testImplementation(libs.mockk.io)
    testImplementation(libs.turbine.test)
    testImplementation(libs.bundles.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.bundles.koin.test)
    androidTestImplementation(libs.bundles.test.core)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
}

koinCompiler {
    compileSafety = false
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
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
    }
}
