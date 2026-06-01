plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.hotswan.compiler)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.targaryen.cafeteria.coredatabase"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(libs.koin)
    implementation(libs.koin.annotation)

    testImplementation(libs.junit)
    testImplementation(libs.mockk.io)
    testImplementation(libs.turbine.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.coroutines.test)
}

koinCompiler {
    compileSafety = false
}
