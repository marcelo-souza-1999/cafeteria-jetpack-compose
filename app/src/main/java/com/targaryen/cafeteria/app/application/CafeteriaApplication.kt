package com.targaryen.cafeteria.app.application

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.targaryen.cafeteria.app.BuildConfig
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.plugin.module.dsl.startKoin

class CafeteriaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val appCheckProviderFactory =
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(appCheckProviderFactory)

        startKoin<CafeteriaKoinApp> {
            androidLogger()
            androidContext(this@CafeteriaApplication)
        }

        val crashlytics = get<FirebaseCrashlytics>()
        crashlytics.isCrashlyticsCollectionEnabled = true
        crashlytics.setCustomKey("environment", BuildConfig.ENVIRONMENT_NAME)
        crashlytics.log("app_started env=${BuildConfig.ENVIRONMENT_NAME}")
    }
}
