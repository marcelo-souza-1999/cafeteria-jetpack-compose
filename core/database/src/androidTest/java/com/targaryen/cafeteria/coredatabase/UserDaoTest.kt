package com.targaryen.cafeteria.coredatabase

import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.targaryen.cafeteria.coredatabase.dao.UserDao
import com.targaryen.cafeteria.coredatabase.model.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var database: TargaryenDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TargaryenDatabase::class.java
        ).allowMainThreadQueries().build()
        userDao = database.userDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertUserAndGetUserByUid() = runTest {
        val user = UserEntity(
            uid = "user_123",
            name = "Rhaenyra Targaryen",
            email = "queen@dragonstone.com"
        )
        userDao.insertUser(user)

        val retrievedUser = userDao.getUserByUid(user.uid).first()
        assertEquals(user, retrievedUser)
    }

    @Test
    fun deleteUserByUid() = runTest {
        val user = UserEntity(
            uid = "user_456",
            name = "Daemon Targaryen",
            email = "daemon@dragonstone.com"
        )
        userDao.insertUser(user)
        userDao.deleteUserByUid(user.uid)

        val retrievedUser = userDao.getUserByUid(user.uid).first()
        assertNull(retrievedUser)
    }

    @Test
    fun replaceOnConflict() = runTest {
        val user1 = UserEntity(uid = "1", name = "Viserys", email = "v1@t.com")
        val user2 = UserEntity(uid = "1", name = "Viserys II", email = "v2@t.com")

        userDao.insertUser(user1)
        userDao.insertUser(user2)

        val retrievedUser = userDao.getUserByUid("1").first()
        assertEquals("Viserys II", retrievedUser?.name)
        assertEquals("v2@t.com", retrievedUser?.email)
    }
}
