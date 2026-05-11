package com.targaryen.cafeteria.app

import com.targaryen.cafeteria.feature.auth.di.AuthModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [AuthModule::class])
@ComponentScan("com.targaryen.cafeteria.app")
class AppModule
