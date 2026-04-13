package isel.pt.cbdcg.domain

data class AuthUser (
    val token: String,
    val email: Email,
    val name: Name
)