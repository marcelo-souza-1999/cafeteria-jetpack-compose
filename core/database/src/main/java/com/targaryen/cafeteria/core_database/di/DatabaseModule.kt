package com.targaryen.cafeteria.core_database.di

import android.content.Context
import androidx.room3.Room
import com.targaryen.cafeteria.core_database.TargaryenDatabase
import com.targaryen.cafeteria.core_database.dao.UserDao
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.targaryen.cafeteria.core_database")
class DatabaseModule {

    @Single
    fun provideDatabase(context: Context): TargaryenDatabase {
        return Room.databaseBuilder<TargaryenDatabase>(
            context = context,
            name = "targaryen_database"
        ).build()
    }

    @Single
    fun provideUserDao(database: TargaryenDatabase): UserDao {
        return database.userDao()
    }
}
