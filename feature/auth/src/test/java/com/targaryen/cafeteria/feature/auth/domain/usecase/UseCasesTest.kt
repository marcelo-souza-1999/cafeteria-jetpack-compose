package com.targaryen.cafeteria.feature.auth.domain.usecase

import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class UseCasesTest {
    private val repository: AuthRepository = mockk()

    @Test
    fun `LogoutUseCase should delegate to repository`() =
        runTest {
            coEvery { repository.logout() } returns Unit

            LogoutUseCase(repository)()

            coVerify { repository.logout() }
        }

    @Test
    fun `SignInWithEmailUseCase should delegate to repository`() =
        runTest {
            val email = "test@test.com"
            val password = "pass"
            every { repository.signInWithEmail(email, password) } returns flowOf(Resource.Success(Unit))

            SignInWithEmailUseCase(repository)(email, password).collect()

            verify { repository.signInWithEmail(email, password) }
        }

    @Test
    fun `SignInWithGoogleUseCase should delegate to repository`() =
        runTest {
            val token = "token"
            every { repository.signInWithGoogle(token) } returns flowOf(Resource.Success(Unit))

            SignInWithGoogleUseCase(repository)(token).collect()

            verify { repository.signInWithGoogle(token) }
        }

    @Test
    fun `CheckAuthSessionUseCase should delegate to repository`() =
        runTest {
            coEvery { repository.isUserLoggedIn() } returns true

            val result = CheckAuthSessionUseCase(repository)()

            assertTrue(result)
            verify { repository.isUserLoggedIn() }
        }

    @Test
    fun `SendPasswordResetEmailUseCase should delegate to repository`() =
        runTest {
            val email = "reset@test.com"
            every { repository.sendPasswordResetEmail(email) } returns flowOf(Resource.Success(Unit))

            SendPasswordResetEmailUseCase(repository)(email).collect()

            verify { repository.sendPasswordResetEmail(email) }
        }

    @Test
    fun `SignUpWithEmailUseCase should delegate to repository`() =
        runTest {
            val name = "Test User"
            val email = "test@test.com"
            val password = "pass"
            every { repository.signUpWithEmail(name, email, password) } returns flowOf(Resource.Success(Unit))

            SignUpWithEmailUseCase(repository)(name, email, password).collect()

            verify { repository.signUpWithEmail(name, email, password) }
        }
}
