package com.mandor.kmp.persistance

import com.mandor.kmp.persistance.internal.CryptoProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Serializable
data class TestData(val message: String)

class MockCryptoProvider : CryptoProvider {
    override fun encrypt(data: ByteArray): ByteArray {
        return data.map { (it + 1).toByte() }.toByteArray() // Simple shift
    }

    override fun decrypt(encryptedData: ByteArray): ByteArray {
        return encryptedData.map { (it - 1).toByte() }.toByteArray() // Simple unshift
    }
}

class SecureSerializerTest {

    @Test
    fun testEncryptDecrypt() = runTest {
        val cryptoProvider = MockCryptoProvider()
        val serializer = SecureSerializer(TestData.serializer(), cryptoProvider, TestData("default"))
        val data = TestData("Hello World")

        val buffer = okio.Buffer()
        serializer.writeTo(data, buffer)
        
        val encryptedBytes = buffer.readByteArray()
        // Verify it's not plain JSON (starts with '{')
        // "Hello World" JSON is {"message":"Hello World"}
        // First char '{' is 123. Encrypted should be 124.
        assertEquals(124.toByte(), encryptedBytes[0])

        val inputBuffer = okio.Buffer().write(encryptedBytes)
        val result = serializer.readFrom(inputBuffer)
        
        assertEquals(data, result)
    }
}
