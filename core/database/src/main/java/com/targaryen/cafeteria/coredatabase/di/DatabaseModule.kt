package com.targaryen.cafeteria.coredatabase.di

import android.content.Context
import androidx.room3.Room
import com.targaryen.cafeteria.coredatabase.dao.ChatDao
import com.targaryen.cafeteria.coredatabase.dao.ProductDao
import com.targaryen.cafeteria.coredatabase.dao.UserDao
import com.targaryen.cafeteria.coredatabase.database.TargaryenDatabase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.targaryen.cafeteria.coredatabase")
class DatabaseModule {
    @Single
    fun provideDatabase(context: Context): TargaryenDatabase =
        Room
            .databaseBuilder<TargaryenDatabase>(
                context = context,
                name = "targaryen_database",
            ).fallbackToDestructiveMigration()
            .build()

    @Single
    fun provideUserDao(database: TargaryenDatabase): UserDao = database.userDao()

    @Single
    fun provideProductDao(database: TargaryenDatabase): ProductDao = database.productDao()

    @Single
    fun provideChatDao(database: TargaryenDatabase): ChatDao = database.chatDao()
}
