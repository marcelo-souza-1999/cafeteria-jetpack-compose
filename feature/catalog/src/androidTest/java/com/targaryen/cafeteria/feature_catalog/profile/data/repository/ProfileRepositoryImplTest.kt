package com.targaryen.cafeteria.feature_catalog.profile.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.targaryen.cafeteria.coredatabase.dao.UserDao
import com.targaryen.cafeteria.coredatabase.model.UserEntity
import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileRepositoryImplTest {

    private val userDao: UserDao = mockk(relaxed = true)
    private val firebaseAuth: FirebaseAuth = mockk()
    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val firebaseStorage: FirebaseStorage = mockk()
    
    private val mockUser: FirebaseUser = mockk()

    private lateinit var repository: ProfileRepositoryImpl

    @Before
    fun setup() {
        every { mockUser.uid } returns "123"
        every { mockUser.displayName } returns "Aegon"
        every { mockUser.email } returns "aegon@targaryen.com"
        
        repository = ProfileRepositoryImpl(
            userDao = userDao,
            firebaseAuth = firebaseAuth,
            firestore = firestore,
            firebaseStorage = firebaseStorage
        )
    }

    @Test
    fun getProfile_shouldEmitUserNotFound_whenUserIsNull() = runTest {
        every { firebaseAuth.currentUser } returns null

        val result = repository.getProfile().first()

        assertTrue(result is Resource.Error)
        assertEquals(ProfileError.UserNotFound, (result as Resource.Error).error)
    }

    @Test
    fun updateProfileName_shouldUpdateFirestoreAndLocalDao() = runTest {
        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.updateProfile(any()) } returns Tasks.forResult(null)
        
        val docRef: DocumentReference = mockk()
        every { firestore.collection("users").document("123") } returns docRef
        every { docRef.update("name", "New Name") } returns Tasks.forResult(null)
        
        val localUser = UserEntity("123", "Aegon", "aegon@targaryen.com")
        every { userDao.getUserByUid("123") } returns flowOf(localUser)
        coEvery { userDao.insertUser(any()) } returns Unit

        val result = repository.updateProfileName("New Name").first()

        assertTrue(result is Resource.Success)
        coVerify { userDao.insertUser(localUser.copy(name = "New Name")) }
    }
}
