package com.mandor.kmp.persistance.internal

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class IosCryptoProviderTest {

    @Test
    fun testEncryptionDecryption() {
        val cryptoProvider = IOSCryptoProvider()
        val originalData = "Hello, World!".encodeToByteArray()

        val encryptedData = cryptoProvider.encrypt(originalData)
        
        // Verify that data is actually encrypted (not equal to original)
        assertFalse(originalData.contentEquals(encryptedData), "Encrypted data should not match original data")
        
        val decryptedData = cryptoProvider.decrypt(encryptedData)

        // Verify round-trip
        assertTrue(originalData.contentEquals(decryptedData), "Decrypted data should match original data")
    }
}
