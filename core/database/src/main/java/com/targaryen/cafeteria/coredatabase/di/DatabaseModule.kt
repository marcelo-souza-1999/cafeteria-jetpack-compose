package com.targaryen.cafeteria.coredatabase.di

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.targaryen.cafeteria.coredatabase.dao.ChatDao
import com.targaryen.cafeteria.coredatabase.dao.ProductDao
import com.targaryen.cafeteria.coredatabase.dao.UserDao
import com.targaryen.cafeteria.coredatabase.database.TargaryenDatabase
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.targaryen.cafeteria.coredatabase")
class DatabaseModule {
    @Single
    fun provideDatabase(context: Context): TargaryenDatabase {
        val dbBuilder =
            Room
                .databaseBuilder<TargaryenDatabase>(
                    context = context,
                    name = "targaryen_database",
                ).addMigrations(MIGRATION_4_5)
                .fallbackToDestructiveMigration()

        val db = dbBuilder.build()

        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return if (isDebuggable) {
            try {
                runBlocking {
                    db.userDao().deleteUserByUid("")
                }
                db
            } catch (e: IllegalStateException) {
                recreateDatabase(context, dbBuilder, e)
            } catch (e: android.database.sqlite.SQLiteException) {
                recreateDatabase(context, dbBuilder, e)
            }
        } else {
            db
        }
    }

    private fun recreateDatabase(
        context: Context,
        dbBuilder: RoomDatabase.Builder<TargaryenDatabase>,
        e: Throwable,
    ): TargaryenDatabase {
        Log.w(
            "DatabaseModule",
            "Schema integrity check failed. Recreating database in debug mode.",
            e,
        )
        context.deleteDatabase("targaryen_database")
        val cleanDb = dbBuilder.build()
        runBlocking {
            try {
                cleanDb.userDao().deleteUserByUid("")
            } catch (cleanException: IllegalStateException) {
                Log.e(
                    "DatabaseModule",
                    "Failed to open clean database.",
                    cleanException,
                )
            } catch (cleanException: android.database.sqlite.SQLiteException) {
                Log.e(
                    "DatabaseModule",
                    "Failed to open clean database.",
                    cleanException,
                )
            }
        }
        return cleanDb
    }

    @Single
    fun provideUserDao(database: TargaryenDatabase): UserDao = database.userDao()

    @Single
    fun provideProductDao(database: TargaryenDatabase): ProductDao = database.productDao()

    @Single
    fun provideChatDao(database: TargaryenDatabase): ChatDao = database.chatDao()

    companion object {
        private val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override suspend fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `chat_messages_new` (
                            `id` TEXT NOT NULL, 
                            `sessionId` TEXT NOT NULL, 
                            `text` TEXT NOT NULL, 
                            `sender` TEXT NOT NULL, 
                            `timestamp` INTEGER NOT NULL, 
                            PRIMARY KEY(`id`), 
                            FOREIGN KEY(`sessionId`) REFERENCES `chat_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    connection.execSQL(
                        """
                        INSERT INTO `chat_messages_new` (`id`, `sessionId`, `text`, `sender`, `timestamp`)
                        SELECT `id`, `sessionId`, `text`, `sender`, `timestamp` FROM `chat_messages`
                        """.trimIndent(),
                    )

                    connection.execSQL("DROP TABLE `chat_messages`")
                    connection.execSQL("ALTER TABLE `chat_messages_new` RENAME TO `chat_messages`")
                    connection.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_chat_messages_sessionId` ON `chat_messages` (`sessionId`)",
                    )
                }
            }
    }
}
