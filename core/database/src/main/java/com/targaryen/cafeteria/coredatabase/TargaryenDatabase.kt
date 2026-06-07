package com.targaryen.cafeteria.coredatabase

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.targaryen.cafeteria.coredatabase.dao.ProductDao
import com.targaryen.cafeteria.coredatabase.dao.UserDao
import com.targaryen.cafeteria.coredatabase.model.ProductEntity
import com.targaryen.cafeteria.coredatabase.model.UserEntity

@Database(
    entities = [UserEntity::class, ProductEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class TargaryenDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun productDao(): ProductDao
}
