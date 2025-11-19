package com.mandor.kmp.persistance.internal

interface CryptoProvider {
    fun encrypt(data: ByteArray): ByteArray
    fun decrypt(encryptedData: ByteArray): ByteArray
}

expect fun getCryptoProvider(): CryptoProvider

class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)