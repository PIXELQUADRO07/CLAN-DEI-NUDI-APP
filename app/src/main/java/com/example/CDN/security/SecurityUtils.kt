package com.example.CDN.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

object SecurityUtils {

    /**
     * Genera una chiave AES a 256 bit a partire da una stringa.
     */
    private fun generateKey(password: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = password.toByteArray(StandardCharsets.UTF_8)
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()
        return SecretKeySpec(key, "AES")
    }

    /**
     * Cifra una stringa usando AES-256 con una password fornita.
     */
    fun encryptAES(text: String, password: String): String {
        return try {
            val secretKey = generateKey(password)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedBytes = cipher.doFinal(text.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim()
        } catch (e: Exception) {
            "ERR_CRYPT"
        }
    }

    /**
     * Decifra una stringa base64 AES-256 con una password fornita.
     */
    fun decryptAES(encryptedBase64: String, password: String): String {
        return try {
            val secretKey = generateKey(password)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decodedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)
            val decryptedString = String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8)
            decryptedString
        } catch (e: Exception) {
            "/// DATI CORROTTI O CHIAVE ERRATA ///"
        }
    }

    // Simple reversible XOR encryption using a dynamic key for the Clan E2E simulation.
    // This turns plain message strings into encrypted Base64 and shows hexadecimal decrypt dumps in UI.
    fun encrypt(plainText: String, key: String): String {
        try {
            val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
            val textBytes = plainText.toByteArray(StandardCharsets.UTF_8)
            val encrypted = ByteArray(textBytes.size)
            for (i in textBytes.indices) {
                encrypted[i] = (textBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            return Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            return "ERROR_ENCRYPTION_FAIL"
        }
    }

    fun decrypt(encryptedBase64: String, key: String): String {
        try {
            val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
            val encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val decrypted = ByteArray(encryptedBytes.size)
            for (i in encryptedBytes.indices) {
                decrypted[i] = (encryptedBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            return String(decrypted, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            return "[CORRUPTED DECRYPTION SEED]"
        }
    }

    // Hex dumps for that sweet Cyberpunk matrix UI look!
    fun stringToHexDump(text: String): String {
        val hexChars = "0123456789ABCDEF"
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        val result = StringBuilder()
        for (i in bytes.indices) {
            val b = bytes[i].toInt() and 0xff
            result.append(hexChars[b shr 4])
            result.append(hexChars[b and 0x0f])
            if (i < bytes.size - 1) result.append(" ")
            if ((i + 1) % 8 == 0) result.append("\n")
        }
        return result.toString()
    }
}
