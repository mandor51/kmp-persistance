package com.mandor.kmp.persistance.internal

interface CryptoProvider {
    fun encrypt(data: String): String
    fun decrypt(encryptedData: String): String
}

expect fun getCryptoProvider(): CryptoProvider