package com.targaryen.cafeteria.coredatabase.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.targaryen.cafeteria.coredatabase.dao.ChatDao
import com.targaryen.cafeteria.coredatabase.dao.ProductDao
import com.targaryen.cafeteria.coredatabase.dao.UserDao
import com.targaryen.cafeteria.coredatabase.model.ChatMessageEntity
import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import com.targaryen.cafeteria.coredatabase.model.ProductEntity
import com.targaryen.cafeteria.coredatabase.model.UserEntity

@Database(
    entities = [UserEntity::class, ProductEntity::class, ChatMessageEntity::class, ChatSessionEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class TargaryenDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun productDao(): ProductDao

    abstract fun chatDao(): ChatDao
}
