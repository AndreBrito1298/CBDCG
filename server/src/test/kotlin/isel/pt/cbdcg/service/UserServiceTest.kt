package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserServiceTest {

    private val userRepo = UserRepositoryMem
    private val userService = UserService(userRepo)

    @BeforeTest
    fun clearRepo() = runBlocking {
        userRepo.clear()
    }

    @Test
    fun `create user authenticates returned user and stores encrypted credentials`() = runBlocking {
        val user = userService.createUser(
            Name("testName"),
            Email("testEmail@gmail.com"),
            Password("testPassword"),
        ).getOrThrow()

        val stored = userRepo.findById(user.id)

        assertEquals("testEmail@gmail.com", user.email.string)
        assertNotNull(user.auth)
        assertTrue(user.auth!!.token.isNotBlank())
        assertNotNull(stored)
        assertEquals(SimpleCrypto.encrypt("testPassword"), stored.password.string)
        assertEquals(SimpleCrypto.encrypt(user.auth!!.token), stored.auth?.token)
    }

    @Test
    fun `duplicate email fails`(): Unit = runBlocking {
        userService.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword")).getOrThrow()

        assertFailsWith<UserError.DuplicateEmail> {
            userService.createUser(
                Name("otherName"),
                Email("testEmail@gmail.com"),
                Password("otherPassword"),
            ).getOrThrow()
        }
    }

    @Test
    fun `login succeeds for existing unauthenticated user`() = runBlocking {
        userRepo.createUser(
            Name("testName"),
            Email("testEmail@gmail.com"),
            Password(SimpleCrypto.encrypt("testPassword")),
        )

        val logged = userService.login(Email("testEmail@gmail.com"), Password("testPassword")).getOrThrow()
        val stored = userRepo.findById(logged.id)

        assertNotNull(logged.auth)
        assertEquals(SimpleCrypto.encrypt(logged.auth!!.token), stored?.auth?.token)
    }

    @Test
    fun `login fails when email is not found`(): Unit = runBlocking {
        assertFailsWith<UserError.EmailNotFound> {
            userService.login(Email("randomEmail@gmail.com"), Password("randomPassword")).getOrThrow()
        }
    }

    @Test
    fun `login fails when password is incorrect`(): Unit = runBlocking {
        userRepo.createUser(
            Name("testName"),
            Email("testEmail@gmail.com"),
            Password(SimpleCrypto.encrypt("testPassword")),
        )

        assertFailsWith<UserError.PasswordMismatch> {
            userService.login(Email("testEmail@gmail.com"), Password("wrongPassword")).getOrThrow()
        }
    }

    @Test
    fun `login fails when user is already logged in`(): Unit = runBlocking {
        val authenticated = userService.createUser(
            Name("testName"),
            Email("testEmail@gmail.com"),
            Password("testPassword"),
        ).getOrThrow()

        assertFailsWith<UserError.AlreadyLoggedIn> {
            userService.login(authenticated.email, Password("testPassword")).getOrThrow()
        }
    }

    @Test
    fun `logout clears authentication token`() = runBlocking {
        val authenticated = userService.createUser(
            Name("testName"),
            Email("testEmail@gmail.com"),
            Password("testPassword"),
        ).getOrThrow()

        userService.logout(authenticated.auth!!.token).getOrThrow()

        val stored = userRepo.findByEmail(authenticated.email)
        assertNotNull(stored)
        assertNull(stored.auth)
    }

    @Test
    fun `logout fails when token does not exist`(): Unit = runBlocking {
        assertFailsWith<UserError.TokenNotFound> {
            userService.logout("missing-token").getOrThrow()
        }
    }
}
