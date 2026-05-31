import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.hotswan.compiler)
    alias(libs.plugins.google.gms.services)
}

android {
    namespace = "com.targaryen.cafeteria.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.targaryen.cafeteria.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
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
    implementation(project(":feature:cart"))
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

    ksp(libs.koin.ksp.compiler)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.compose.icons)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.coil)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.generativeai)

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

ksp {
    arg("KOIN_DEFAULT_MODULE", "true")
    arg("KOIN_CONFIG_CHECK", "true")
    arg("KOIN_ANNOTATIONS_ROOT_PACKAGE", "com.targaryen.cafeteria.app")
}

tasks.register("detektAll") {
    dependsOn(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>())
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
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
    }
}
