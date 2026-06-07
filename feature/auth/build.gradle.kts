plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hotswan.compiler)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.targaryen.cafeteria.feature.auth"
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

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = false
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

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xexplicit-backing-fields",
                "-opt-in=kotlin.ExperimentalStdlibApi",
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST}"
        }
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.bundles.compose.icons)

    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.navigation)

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
