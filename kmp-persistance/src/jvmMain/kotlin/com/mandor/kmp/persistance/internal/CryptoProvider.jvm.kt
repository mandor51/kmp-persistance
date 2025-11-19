package com.mandor.kmp.persistance.internal

actual fun getCryptoProvider(): CryptoProvider {
    return JVMCryptoProvider()
}

class JVMCryptoProvider : CryptoProvider {

    // This is mock implementation for JVM platform and to test purposes only.

    override fun encrypt(data: ByteArray) = data

    override fun decrypt(encryptedData: ByteArray) = encryptedData
}