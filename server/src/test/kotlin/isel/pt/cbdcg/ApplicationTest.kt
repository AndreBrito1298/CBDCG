package isel.pt.cbdcg

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @BeforeTest
    fun clearState() {
        UserRepositoryMem.clear()
        TableRepositoryMem.clear()
        ParticipantRepositoryMem.participants.clear()
    }

    @Test
    fun `create user endpoint returns created`() = testApplication {
        application { module() }

        val response = client.post("/auth/users/create") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Alice","email":"alice@gmail.com","password":"secret1"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `create user endpoint maps duplicate email to conflict`() = testApplication {
        application { module() }

        client.post("/auth/users/create") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Alice","email":"alice@gmail.com","password":"secret1"}""")
        }

        val response = client.post("/auth/users/create") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Alice","email":"alice@gmail.com","password":"secret1"}""")
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `login endpoint returns created for unauthenticated stored user`() = testApplication {
        UserRepositoryMem.createUser(Name("Alice"), Email("alice@gmail.com"), Password("secret1"))
        application { module() }

        val response = client.post("/auth/users/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"alice@gmail.com","password":"secret1"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `logout endpoint returns not found for missing token`() = testApplication {
        application { module() }

        val response = client.post("/auth/users/logout") {
            contentType(ContentType.Application.Json)
            setBody("""{"token":"missing-token"}""")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `get tables endpoint returns ok`() = testApplication {
        application { module() }

        val response = client.get("/tables")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `invalid create user body returns bad request`() = testApplication {
        application { module() }

        val response = client.post("/auth/users/create") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Alice"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
