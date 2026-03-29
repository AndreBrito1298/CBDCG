package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.service.UserService
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserRepositoryMemTest {

    private val userRepo = UserRepositoryMem
    private val userService = UserService(userRepo)

    @BeforeTest
    fun clearRepo() {
        userRepo.clear()
    }

    @Test
    fun `create user successfully`() {

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")

        userService.createUser(name, email, password)
        assert(userRepo.users.find{ it.email.string == email.string } != null)

    }


    @Test
    fun `create user with same name or password`() {

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        userService.createUser(name, email, password)

        val newEmail = Email("randomEmail@gmail.com")

        userService.createUser(name, newEmail, password)
        assert(userRepo.users.find{ it.email.string == newEmail.string } != null)

    }

    @Test
    fun `login successfully`() {

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        val user = userService.createUser(name, email, password)

        val loggedInUser = userService.login(email, password).getOrNull()
        assert(loggedInUser != null)
        assertEquals(loggedInUser!!.email, user.getOrNull()!!.email)
    }



    @Test
    fun `user can be found by email`(){

        val name = Name("testName")
        val email = Email("testEmail@gmail.com")
        val password = Password("testPassword")
        userRepo.createUser(name, email, password)

        assert(userRepo.findByEmail(email)!!.email.string == email.string)

    }

    @Test
    fun `user cannot be found by email, because it does not exist`(){
        assertNull(userRepo.findByEmail(Email("randomEmail@gmail.com")))
    }

}