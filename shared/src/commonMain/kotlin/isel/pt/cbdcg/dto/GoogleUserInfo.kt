package isel.pt.cbdcg.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleUserInfo(
    val id: String,
    val email: String,
    val name: String,
    @SerialName("picture")
    val pictureUrl: String? = null
)