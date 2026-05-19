package com.targaryen.cafeteria.app

import android.content.Context
import com.targaryen.cafeteria.coredatabase.di.DatabaseModule
import com.targaryen.cafeteria.feature.auth.di.AuthModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module(includes = [AuthModule::class, DatabaseModule::class])
@ComponentScan("com.targaryen.cafeteria.app")
class AppModule {
    @Single
    @Named("WebClientId")
    fun provideWebClientId(context: Context): String {
        return context.getString(R.string.default_web_client_id)
    }
}
