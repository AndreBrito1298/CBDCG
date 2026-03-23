package isel.pt.cbdcg.repository

import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserRepositoryMemTest {

    private val userRepo = UserRepositoryMem

    @BeforeTest
    fun clearRepo() {
        userRepo.clear()
    }

    @Test
    fun `create user successfully`() {

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")

        userRepo.createUser(name, email, password)
        assert(userRepo.users.find{ it.email.string == email.string } != null)

    }

    @Test
    fun `create user with duplicate email`() {

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        userRepo.createUser(name, email, password)

        assertFailsWith<UserError.DuplicateEmail> {
            userRepo.createUser(
                Name("randomName"),
                email,
                Password("randomPassword"))
        }

    }

    @Test
    fun `create user with same name or password`() {

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        userRepo.createUser(name, email, password)

        val newEmail = Email("randomEmail@gmail.com")

        userRepo.createUser(name, newEmail, password)
        assert(userRepo.users.find{ it.email.string == newEmail.string } != null)

    }

    @Test
    fun `login successfully`() {

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        val user = userRepo.createUser(name, email, password)

        assertEquals(userRepo.login(email, password), user)

    }

    @Test
    fun `user cannot login, because email was not found`() {

        assertFailsWith<UserError.EmailNotFound> {
            userRepo.login(Email("randomEmail@gmail.com"), Password("randomPassword"))
        }

    }

    @Test
    fun `user cannot login, wrong password`() {

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        userRepo.createUser(name, email, password)

        assertFailsWith<UserError.PasswordMismatch> {
            userRepo.login(email, Password("randomPassword"))
        }

    }

    @Test
    fun `user can be found by email`(){

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        userRepo.createUser(name, email, password)

        assert(userRepo.findByEmail(email).email.string == email.string)

    }

    @Test
    fun `user cannot be found by email, because it does not exist`(){

        assertFailsWith<UserError.EmailNotFound> {
            userRepo.findByEmail(Email("randomEmail@gmail.com"))
        }
    }

}