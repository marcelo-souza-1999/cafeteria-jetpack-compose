package com.targaryen.cafeteria.coredatabase.di

import android.content.Context
import androidx.room3.Room
import com.targaryen.cafeteria.coredatabase.TargaryenDatabase
import com.targaryen.cafeteria.coredatabase.dao.UserDao
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.targaryen.cafeteria.coredatabase")
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
