package com.mandor.kmp.persistance

import androidx.datastore.core.okio.OkioSerializer
import com.mandor.kmp.persistance.internal.CryptoProvider
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import okio.use

class SecureSerializer<T>(
    private val serializer: KSerializer<T>,
    private val cryptoProvider: CryptoProvider,
    override val defaultValue: T,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : OkioSerializer<T> {

    override suspend fun readFrom(source: BufferedSource): T {
        val encryptedBytes = source.readByteArray()
        if (encryptedBytes.isEmpty()) {
             return defaultValue
        }
        val decryptedBytes = cryptoProvider.decrypt(encryptedBytes)
        val string = decryptedBytes.decodeToString()
        return json.decodeFromString(serializer, string)
    }

    override suspend fun writeTo(t: T, sink: BufferedSink) {
        val string = json.encodeToString(serializer, t)
        val bytes = string.encodeToByteArray()
        val encryptedBytes = cryptoProvider.encrypt(bytes)
        sink.write(encryptedBytes)
    }
}
