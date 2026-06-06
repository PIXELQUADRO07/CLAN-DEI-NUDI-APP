package com.example.CDN.security

import android.util.Base64
import java.nio.charset.StandardCharsets

object SecurityUtils {

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
