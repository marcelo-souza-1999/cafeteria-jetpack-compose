package com.targaryen.cafeteria.coredatabase

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.targaryen.cafeteria.coredatabase.dao.UserDao
import com.targaryen.cafeteria.coredatabase.model.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TargaryenDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
