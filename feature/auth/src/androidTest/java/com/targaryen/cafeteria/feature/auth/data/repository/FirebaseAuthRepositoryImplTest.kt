package com.targaryen.cafeteria.feature.auth.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.coredatabase.dao.ProductDao
import com.targaryen.cafeteria.coredatabase.dao.UserDao
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirebaseAuthRepositoryImplTest {
    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val userDao: UserDao = mockk(relaxed = true)
    private val productDao: ProductDao = mockk(relaxed = true)
    private lateinit var repository: FirebaseAuthRepositoryImpl

    @Before
    fun setup() {
        repository =
            FirebaseAuthRepositoryImpl(
                firebaseAuth = firebaseAuth,
                firestore = firestore,
                userDao = userDao,
                productDao = productDao,
            )
    }

    @Test
    fun isUserLoggedIn_shouldReturnTrue_whenCurrentUserIsNotNull() {
        every { firebaseAuth.currentUser } returns mockk()
        assertTrue(repository.isUserLoggedIn())
    }

    @Test
    fun isUserLoggedIn_shouldReturnFalse_whenCurrentUserIsNull() {
        every { firebaseAuth.currentUser } returns null
        assertFalse(repository.isUserLoggedIn())
    }

    @Test
    fun signInWithEmail_shouldEmitSuccess_whenFirebaseSucceeds() =
        runTest {
            val authResult = mockk<AuthResult>()
            val mockUser = mockk<FirebaseUser>(relaxed = true)
            every { authResult.user } returns mockUser

            every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns
                Tasks.forResult(
                    authResult,
                )

            val result = repository.signInWithEmail("test@test.com", "password").first()

            assertTrue(result is Resource.Success)
        }

    @Test
    fun signInWithEmail_shouldEmitInvalidCredentials_whenFirebaseThrowsInvalidCredentials() =
        runTest {
            val exception = FirebaseAuthInvalidCredentialsException("error", "message")
            every {
                firebaseAuth.signInWithEmailAndPassword(
                    any(),
                    any(),
                )
            } returns Tasks.forException(exception)

            val result = repository.signInWithEmail("test@test.com", "password").first()

            assertTrue(result is Resource.Error)
            assertEquals(AuthError.InvalidCredentials, (result as Resource.Error).error)
        }

    @Test
    fun signInWithEmail_shouldEmitUserNotFound_whenFirebaseThrowsInvalidUser() =
        runTest {
            val exception = FirebaseAuthInvalidUserException("error", "message")
            every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns
                Tasks.forException(
                    exception,
                )

            val result = repository.signInWithEmail("test@test.com", "password").first()

            assertTrue(result is Resource.Error)
            assertEquals(AuthError.UserNotFound, (result as Resource.Error).error)
        }

    @Test
    fun signInWithEmail_shouldEmitNetworkError_whenFirebaseThrowsNetworkException() =
        runTest {
            val exception = FirebaseNetworkException("no connection")
            every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns
                Tasks.forException(
                    exception,
                )

            val result = repository.signInWithEmail("test@test.com", "password").first()

            assertTrue(result is Resource.Error)
            assertEquals(AuthError.NetworkError, (result as Resource.Error).error)
        }

    @Test
    fun signInWithEmail_shouldEmitUnknownError_whenFirebaseThrowsGenericException() =
        runTest {
            val exception = Exception("generic error")
            every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns
                Tasks.forException(
                    exception,
                )

            val result = repository.signInWithEmail("test@test.com", "password").first()

            assertTrue(result is Resource.Error)
            val error = (result as Resource.Error).error
            assertTrue(error is AuthError.Unknown)
            assertEquals("generic error", (error as AuthError.Unknown).message)
        }

    @Test
    fun sendPasswordResetEmail_shouldEmitSuccess_whenFirebaseSucceeds() =
        runTest {
            every { firebaseAuth.sendPasswordResetEmail(any()) } returns Tasks.forResult(null)

            val result = repository.sendPasswordResetEmail("test@test.com").first()

            assertTrue(result is Resource.Success)
        }

    @Test
    fun sendPasswordResetEmail_shouldEmitUserNotFound_whenEmailDoesNotExist() =
        runTest {
            val exception = FirebaseAuthInvalidUserException("error", "message")
            every { firebaseAuth.sendPasswordResetEmail(any()) } returns Tasks.forException(exception)

            val result = repository.sendPasswordResetEmail("nonexistent@test.com").first()

            assertTrue(result is Resource.Error)
            assertEquals(AuthError.UserNotFound, (result as Resource.Error).error)
        }

    @Test
    fun logout_shouldSignOutFromFirebaseAndClearLocalRoomData() =
        runTest {
            val userMock = mockk<FirebaseUser>()
            every { userMock.uid } returns "test_uid"
            every { firebaseAuth.currentUser } returns userMock

            repository.logout()

            verify { firebaseAuth.signOut() }
            coVerify { userDao.deleteUserByUid("test_uid") }
            coVerify { productDao.clearCart() }
        }
}
