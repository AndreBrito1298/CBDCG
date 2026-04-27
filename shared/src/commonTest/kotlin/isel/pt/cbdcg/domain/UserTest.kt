package isel.pt.cbdcg.domain

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class UserTest{

    @Test
    fun `valid name`(){
        assertNotNull(Name("testName"))
        assertNotNull(Name("   " + "A".repeat(19)))
    }

    @Test
    fun `valid string to name`(){
        assertNotNull("testName".toName())
        assertNotNull(("   " + "A".repeat(19)).toName())
    }

    @Test
    fun `valid email`(){
        assertNotNull(Email("testEmail@gmail.com"))
        assertNotNull(Email("t3stEma1l@anyth1ng.eu"))
    }

    @Test
    fun `valid string to email`(){
        assertNotNull("testEmail@gmail.com".toEmail())
        assertNotNull("t3stEma1l@anyth1ng.eu".toEmail())
    }

    @Test
    fun `valid password`(){
        assertNotNull(Password("testPassword"))
    }

    @Test
    fun `valid string to password`(){
        assertNotNull("testPassword".toPassword())
    }

    @Test
    fun `invalid names`(){

        assertFailsWith<IllegalArgumentException>{ Name("") }
        assertFailsWith<IllegalArgumentException>{ Name("    ") }

        assertFailsWith<IllegalArgumentException>{ Name("A".repeat(22)) }
    }

    @Test
    fun `invalid string to name`(){
        assertFailsWith<IllegalArgumentException>{ "".toName() }
        assertFailsWith<IllegalArgumentException>{ "           ".toName() }

        assertFailsWith<IllegalArgumentException>{ "A".repeat(22).toName() }
    }

    @Test
    fun `invalid emails`(){
        assertFailsWith<IllegalArgumentException>{ Email("testEmail@gmail") }
        assertFailsWith<IllegalArgumentException>{ Email("testEmail@gmail.c") }
        assertFailsWith<IllegalArgumentException>{ Email("testEmail@gmail@com") }
        assertFailsWith<IllegalArgumentException>{ Email("testEmail.gmail.com") }
        assertFailsWith<IllegalArgumentException>{ Email("@gmail.com") }
    }

    @Test
    fun `invalid string to email`(){
        assertFailsWith<IllegalArgumentException>{ "testEmail@gmail".toEmail() }
        assertFailsWith<IllegalArgumentException>{ "testEmail@gmail.c".toEmail() }
        assertFailsWith<IllegalArgumentException>{ "testEmail@gmail@com".toEmail() }
        assertFailsWith<IllegalArgumentException>{ "testEmail.gmail.com".toEmail() }
        assertFailsWith<IllegalArgumentException>{ "@gmail.com".toEmail() }
    }

    @Test
    fun `invalid passwords`(){
        assertFailsWith<IllegalArgumentException> { Password("Tiny") }
        assertFailsWith<IllegalArgumentException> { Password("") }
        assertFailsWith<IllegalArgumentException> { Password("          ") }
    }

    @Test
    fun `invalid string to password`(){
        assertFailsWith<IllegalArgumentException> { "Tiny".toPassword() }
        assertFailsWith<IllegalArgumentException> { "".toPassword() }
        assertFailsWith<IllegalArgumentException> { "          ".toPassword() }
    }


}