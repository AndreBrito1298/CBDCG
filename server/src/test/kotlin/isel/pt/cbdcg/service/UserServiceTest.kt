package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
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
    fun clearRepo() {
        userRepo.clear()
    }

    @Test
    fun `create user successfully authenticates and persists token`() {
        val user = userService.createUser(
            Name("testName"),
            Email("testEmail@gmail.com"),
            Password("testPassword"),
        ).getOrThrow()

        assertEquals("testEmail@gmail.com", user.email.string)
        assertNotNull(user.auth)
        assertTrue(user.auth!!.token.isNotBlank())
        assertEquals(user, userRepo.findByToken(user.auth!!.token))
    }

    @Test
    fun `duplicate email fails`() {
        userService.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))

        assertFailsWith<UserError.DuplicateEmail> {
            userService.createUser(
                Name("otherName"),
                Email("testEmail@gmail.com"),
                Password("otherPassword"),
            ).getOrThrow()
        }
    }

    @Test
    fun `login succeeds for existing unauthenticated user`() {
        userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))

        val logged = userService.login(Email("testEmail@gmail.com"), Password("testPassword")).getOrThrow()

        assertNotNull(logged.auth)
        assertEquals(logged, userRepo.findByToken(logged.auth!!.token))
    }

    @Test
    fun `login fails when email is not found`() {
        assertFailsWith<UserError.EmailNotFound> {
            userService.login(Email("randomEmail@gmail.com"), Password("randomPassword")).getOrThrow()
        }
    }

    @Test
    fun `login fails when password is incorrect`() {
        userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))

        assertFailsWith<UserError.PasswordMismatch> {
            userService.login(Email("testEmail@gmail.com"), Password("wrongPassword")).getOrThrow()
        }
    }

    @Test
    fun `login fails when user is already logged in`() {
        val authenticated = userService.createUser(
            Name("testName"),
            Email("testEmail@gmail.com"),
            Password("testPassword"),
        ).getOrThrow()

        assertNotNull(authenticated.auth)
        assertFailsWith<UserError.AlreadyLoggedIn> {
            userService.login(authenticated.email, authenticated.password).getOrThrow()
        }
    }

    @Test
    fun `logout clears authentication token`() {
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
    fun `logout fails when token does not exist`() {
        assertFailsWith<UserError.TokenNotFound> {
            userService.logout("missing-token").getOrThrow()
        }
    }
}
