package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.error.UserError
import java.util.Base64

private const val CRYPTO_KEY = "CBDCG_SIMPLE_KEY"

object SimpleCrypto {

    fun encrypt(value: String): String {
        if (value.isEmpty()) return value
        val input = value.toByteArray(Charsets.UTF_8)
        val key = CRYPTO_KEY.toByteArray(Charsets.UTF_8)
        val encrypted = ByteArray(input.size)
        for (index in input.indices) {
            encrypted[index] = (input[index].toInt() xor key[index % key.size].toInt()).toByte()
        }
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(value: String): String {
        if (value.isEmpty()) return value
        val input = Base64.getDecoder().decode(value)
        val key = CRYPTO_KEY.toByteArray(Charsets.UTF_8)
        val decrypted = ByteArray(input.size)
        for (index in input.indices) {
            decrypted[index] = (input[index].toInt() xor key[index % key.size].toInt()).toByte()
        }
        return decrypted.toString(Charsets.UTF_8)
    }
}
