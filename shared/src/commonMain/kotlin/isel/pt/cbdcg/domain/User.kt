package isel.pt.cbdcg.domain

import com.android.identity.cbor.Uint
import org.h2.command.Token

/**
 * User representation:
 * @property id Unique identifier of the user.
 * @property name Name of the user.
 * @property email Email of the user (Unique).
 * @property password Password of the user.
 */
data class User(
    val id: UInt,
    val name: Name,
    val email: Email,
    val password: Password,
    val creationDate: Long = System.currentTimeMillis(),
    val token: Token? = null,
)