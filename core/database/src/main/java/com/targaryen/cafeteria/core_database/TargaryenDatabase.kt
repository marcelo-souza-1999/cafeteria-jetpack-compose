package com.targaryen.cafeteria.core_database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.targaryen.cafeteria.core_database.dao.UserDao
import com.targaryen.cafeteria.core_database.model.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TargaryenDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
