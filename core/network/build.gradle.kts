import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hotswan.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.koin.compiler)
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            load(FileInputStream(localPropertiesFile))
        }
    }

android {
    namespace = "com.targaryen.cafeteria.core_network"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "B2_KEY_ID", "\"${localProperties.getProperty("B2_KEY_ID") ?: ""}\"")
        buildConfigField(
            "String",
            "B2_APPLICATION_KEY",
            "\"${localProperties.getProperty("B2_APPLICATION_KEY") ?: ""}\"",
        )
        buildConfigField("String", "B2_BUCKET_ID", "\"${localProperties.getProperty("B2_BUCKET_ID") ?: ""}\"")
        buildConfigField("String", "B2_BUCKET_NAME", "\"${localProperties.getProperty("B2_BUCKET_NAME") ?: ""}\"")
        buildConfigField(
            "String",
            "B2_DOWNLOAD_URL_BASE",
            "\"${localProperties.getProperty("B2_DOWNLOAD_URL_BASE") ?: ""}\"",
        )
    }

    buildTypes {
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
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(libs.koin)
    implementation(libs.koin.annotation)

    implementation(libs.bundles.ktor)
    implementation(libs.kotlinx.serialization.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk.io)
    testImplementation(libs.turbine.test)
}

koinCompiler {
    compileSafety = false
}
