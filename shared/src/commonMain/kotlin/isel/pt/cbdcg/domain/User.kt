package isel.pt.cbdcg.domain

import kotlin.time.Clock

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
   // val creationDate: Long = Clock.System
)