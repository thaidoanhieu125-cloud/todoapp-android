package com.example.todoapp.data.repository

import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal object PasswordHash {
    private const val SALT_BYTES = 16
    private val random = SecureRandom()

    private enum class Scheme(val algorithm: String, val iterations: Int, val keyBits: Int) {
        SHA256("PBKDF2WithHmacSHA256", 600_000, 256),
        SHA1("PBKDF2WithHmacSHA1", 1_400_000, 160)
    }

    fun create(password: String): String {
        val scheme = if (Build.VERSION.SDK_INT >= 26) Scheme.SHA256 else Scheme.SHA1
        val salt = ByteArray(SALT_BYTES).also { random.nextBytes(it) }
        return listOf(scheme.name, encode(salt), encode(derive(password, salt, scheme))).joinToString(":")
    }

    fun matches(password: String, stored: String): Boolean {
        val fields = stored.split(':')
        if (fields.size != 3) return false
        val scheme = Scheme.values().firstOrNull { it.name == fields[0] } ?: return false
        if (scheme == Scheme.SHA256 && Build.VERSION.SDK_INT < 26) return false
        return try {
            val salt = Base64.decode(fields[1], Base64.NO_WRAP)
            val expected = Base64.decode(fields[2], Base64.NO_WRAP)
            salt.size == SALT_BYTES && expected.size == scheme.keyBits / 8 &&
                MessageDigest.isEqual(expected, derive(password, salt, scheme))
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun derive(password: String, salt: ByteArray, scheme: Scheme): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, scheme.iterations, scheme.keyBits)
        return try {
            SecretKeyFactory.getInstance(scheme.algorithm).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
}
