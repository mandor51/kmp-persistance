package com.mandor.kmp.persistance.internal

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

actual fun getCryptoProvider(): CryptoProvider {
    return AndroidCryptoProvider()
}

class AndroidCryptoProvider() : CryptoProvider {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    override fun encrypt(data: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val iv = cipher.iv // IV is generated automatically
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            // Combine IV with encrypted data and encode to Base64
            val combined = iv + encryptedBytes
            android.util.Base64.encodeToString(combined, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            throw CryptoException("Encryption failed: ${e.message}", e)
        }
    }

    override fun decrypt(encryptedData: String): String {
        return try {
            val combined = android.util.Base64.decode(encryptedData, android.util.Base64.DEFAULT)
            // First 12 bytes are the IV for GCM
            val iv = combined.copyOfRange(0, 12)
            val encryptedBytes = combined.copyOfRange(12, combined.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            throw CryptoException("Decryption failed: ${e.message}", e)
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        return try {
            val keyAlias = "my_app_secret_key"
            if (keyStore.containsAlias(keyAlias)) {
                keyStore.getKey(keyAlias, null) as SecretKey
            } else {
                val keyGen = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                )
                val spec = KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGen.init(spec)
                keyGen.generateKey()
            }
        } catch (e: Exception) {
            throw CryptoException("Failed to get or create secret key: ${e.message}", e)
        }
    }
}
