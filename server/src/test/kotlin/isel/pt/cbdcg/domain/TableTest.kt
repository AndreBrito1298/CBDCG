package isel.pt.cbdcg.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TableTest {

    private fun user(id: UInt, name: String, email: String) =
        User(id, name.toName(), email.toEmail(), "secret".toPassword())

    @Test
    fun `table is available when it has less than four participants`() {
        val owner = user(1u, "owner", "owner@email.com")
        val participants = listOf(
            Participant(owner, Role.PLAYER),
            Participant(user(2u, "bea", "bea@email.com"), Role.PLAYER),
            Participant(user(3u, "cai", "cai@email.com"), Role.SPECTATOR),
        )

        val table = Table(1u, "testTable".toName(), owner, participants)

        assertTrue(table.checkAvailability())
    }

    @Test
    fun `table is not available when it has four participants`() {
        val owner = user(1u, "owner", "owner@email.com")
        val participants = listOf(
            Participant(owner, Role.PLAYER),
            Participant(user(2u, "bea", "bea@email.com"), Role.PLAYER),
            Participant(user(3u, "cai", "cai@email.com"), Role.PLAYER),
            Participant(user(4u, "dio", "dio@email.com"), Role.SPECTATOR),
        )

        val table = Table(1u, "testTable".toName(), owner, participants)

        assertFalse(table.checkAvailability())
    }
}
