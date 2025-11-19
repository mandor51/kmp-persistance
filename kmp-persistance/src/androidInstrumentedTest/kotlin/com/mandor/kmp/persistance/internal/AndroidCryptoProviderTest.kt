package com.mandor.kmp.persistance.internal

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidCryptoProviderTest {

    @Test
    fun testEncryptionDecryption() {
        try {
            val cryptoProvider = AndroidCryptoProvider()
            val originalData = "Hello, World!".encodeToByteArray()

            val encryptedData = cryptoProvider.encrypt(originalData)
            
            // Verify that data is actually encrypted (not equal to original)
            assertFalse("Encrypted data should not match original data", originalData.contentEquals(encryptedData))
            
            val decryptedData = cryptoProvider.decrypt(encryptedData)

            // Verify round-trip
            assertArrayEquals("Decrypted data should match original data", originalData, decryptedData)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
