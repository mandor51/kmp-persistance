package com.mandor.kmp.persistance.internal

actual fun getCryptoProvider(): CryptoProvider {
    return JVMCryptoProvider()
}

class JVMCryptoProvider: CryptoProvider {
    override fun encrypt(data: String): String {
        // Implement encryption logic for JVM
        return data.reversed() // Placeholder implementation
    }

    override fun decrypt(encryptedData: String): String {
        // Implement decryption logic for JVM
        return encryptedData.reversed() // Placeholder implementation
    }
}