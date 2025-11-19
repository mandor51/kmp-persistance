package com.mandor.kmp.persistance.internal

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertContentEquals

class JvmCryptoProviderTest {

    @Test
    fun testEncryptionDecryption() {
        val cryptoProvider = JVMCryptoProvider()
        val originalData = "Hello, World!".encodeToByteArray()

        val encryptedData = cryptoProvider.encrypt(originalData)
        
        // Verify that data is NOT encrypted (no-op)
        assertContentEquals(originalData, encryptedData, "Encrypted data should match original data for JVM (no-op)")
        
        val decryptedData = cryptoProvider.decrypt(encryptedData)

        // Verify round-trip
        assertContentEquals(originalData, decryptedData, "Decrypted data should match original data")
    }
}
