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

class UserServiceTest {

    private val userRepo = UserRepositoryMem

    private val userService = UserService(userRepo)

    @BeforeTest
    fun clearRepo() {
        userRepo.clear()
    }

    @Test
    fun `create user successfully`(){

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")

        val user1 = userService.createUser(name, email, password)
        assert(user1.isSuccess)

        val newEmail = Email("testEmail@gmail.eu")

        val user2 = userService.createUser(name, newEmail, password)
        assert(user2.isSuccess)

    }

    @Test
    fun `duplicate email`(){

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")

        userService.createUser(name, email, password)

        val newName = Name("testName2")
        val newPassword = Password("testPassword2")

        val user = userService.createUser(newName, email, newPassword)
        assert(user.isFailure)
        assertEquals(user.exceptionOrNull()?.message, "Email '${email.string}' is already in use.")

    }

    @Test
    fun `user logins successfully`(){

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        userService.createUser(name, email, password)

        val login = userService.login(email, password)
        assert(login.isSuccess)
    }

    @Test
    fun `email not found`(){

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")

        val login1 = userService.login(email, password)
        assert(login1.isFailure)
        assertEquals(login1.exceptionOrNull()?.message, "Email '${email.string}' is not bound to any account.")

        userService.createUser(name, email, password)

        val newEmail = Email("testEmail@gmail.eu")

        val login2 = userService.login(newEmail, password)
        assert(login2.isFailure)
        assertEquals(login2.exceptionOrNull()?.message, "Email '${newEmail.string}' is not bound to any account.")

    }

    @Test
    fun `incorrect password`(){

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        userService.createUser(name, email, password)

        val newPassword = Password("testPassword2")

        val login = userService.login(email, newPassword)
        assert(login.isFailure)
        assertEquals(login.exceptionOrNull()?.message, "Passwords do not match.")

    }

    @Test
    fun `user cannot login, wrong password`() {
        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        userRepo.createUser(name, email, password)

        assertFailsWith<UserError.PasswordMismatch> {
            userService.login(email, Password("randomPassword")).getOrThrow()
        }
    }


    @Test
    fun `user cannot login, because email was not found`() {
        assertFailsWith<UserError.EmailNotFound> {
            userService.login(Email("randomEmail@gmail.com"), Password("randomPassword")).getOrThrow()
        }
    }

    @Test
    fun `create user with duplicate email`() {
        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        userService.createUser(name, email, password)

        assertFailsWith<UserError.DuplicateEmail> {
            userService.createUser(
                Name("randomName"),
                email,
                Password("randomPassword")).getOrThrow()
        }

    }

}