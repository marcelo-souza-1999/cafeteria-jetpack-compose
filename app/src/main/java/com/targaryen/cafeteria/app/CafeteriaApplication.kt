package com.targaryen.cafeteria.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.plugin.module.dsl.startKoin

class CafeteriaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<CafeteriaKoinApp> {
            androidLogger()
            androidContext(this@CafeteriaApplication)
        }
    }
}
