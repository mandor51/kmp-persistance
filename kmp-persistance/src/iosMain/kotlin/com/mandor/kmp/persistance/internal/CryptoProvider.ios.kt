package com.mandor.kmp.persistance.internal

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrSynchronizable
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecRandomDefault
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.noErr
import platform.posix.memcpy
import kotlin.experimental.ExperimentalNativeApi

actual fun getCryptoProvider(): CryptoProvider {
    return IOSCryptoProvider()
}

@OptIn(ExperimentalForeignApi::class)
class IOSCryptoProvider : CryptoProvider {

    private val keyAlias = "com.yourcompany.encryption.key"
    private val ivSize = 12 // GCM standard IV size
    private val tagSize = 16 // GCM authentication standard tag size


    @OptIn(ExperimentalForeignApi::class)
    override fun encrypt(data: ByteArray): ByteArray {
        return try {
            val key = getOrCreateKey()
            val iv = generateIV()
            
            // Perform AES-256-GCM encryption
            val (encrypted, tag) = encryptAES256GCM(data, key, iv)

            // Combine IV, encrypted data, and tag for storage
            iv + encrypted + tag
        } catch (e: Exception) {
            throw CryptoException("Encryption failed: ${e.message}", e)
        }
    }

    @OptIn(BetaInteropApi::class)
    override fun decrypt(encryptedData: ByteArray): ByteArray {
        return try {
            val key = getOrCreateKey()
            
            // Extract IV, encrypted data, and authentication tag
            if (encryptedData.size < ivSize + tagSize) {
                throw CryptoException("Invalid encrypted data format", null)
            }

            val iv = encryptedData.copyOfRange(0, ivSize)
            val tag = encryptedData.copyOfRange(encryptedData.size - tagSize, encryptedData.size)
            val encryptedBytes = encryptedData.copyOfRange(ivSize, encryptedData.size - tagSize)

            // Decrypt and verify
            return decryptAES256GCM(encryptedBytes, key, iv, tag)
        } catch (e: Exception) {
            throw CryptoException("Decryption failed: ${e.message}", e)
        }
    }

    private fun getOrCreateKey(): ByteArray {
        // Try to retrieve the key from the Keychain
        val query = CFDictionaryCreateMutable(null, 4, null, null).apply {
            CFDictionaryAddValue(this, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(this, kSecAttrAccount, CFBridgingRetain(keyAlias))
            CFDictionaryAddValue(this, kSecReturnData, kCFBooleanTrue)
            CFDictionaryAddValue(this, kSecMatchLimit, kSecMatchLimitOne)
        }

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            CFRelease(query)
            if (status == noErr.toInt()) {
                val data = CFBridgingRelease(result.value) as NSData
                return data.toByteArray()
            }
        }

        // Key not found, create a new one
        return generateAndStoreKey()
    }

    private fun generateAndStoreKey(): ByteArray {
        val key = ByteArray(32) // 256-bit key
        memScoped {
            val keyPtr = key.usePinned { it.addressOf(0) }
            val status = SecRandomCopyBytes(kSecRandomDefault, key.size.toULong(), keyPtr)

            if (status != noErr.toInt()) {
                throw CryptoException("Failed to generate random key", null)
            }
        }

        val keyData = key.toNSData()
        val addQuery = CFDictionaryCreateMutable(null, 6, null, null).apply {
            CFDictionaryAddValue(this, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(this, kSecAttrAccount, CFBridgingRetain(keyAlias))
            CFDictionaryAddValue(this, kSecValueData, CFBridgingRetain(keyData))
            // Only accessible when device is unlocked
            CFDictionaryAddValue(
                this,
                kSecAttrAccessible,
                kSecAttrAccessibleWhenUnlockedThisDeviceOnly
            )
            // Disable iCloud backup
            CFDictionaryAddValue(this, kSecAttrSynchronizable, kCFBooleanFalse)
        }

        val status = SecItemAdd(addQuery, null)
        CFRelease(addQuery)

        val errSecDup = -25299 // errSecDuplicateItem value
        if (status != noErr.toInt() && status != errSecDup) {
            throw CryptoException("Failed to store key in Keychain: $status", null)
        }

        return key
    }

    private fun generateIV(): ByteArray {
        val iv = ByteArray(ivSize)
        memScoped {
            val ivPtr = iv.usePinned { it.addressOf(0) }
            SecRandomCopyBytes(kSecRandomDefault, iv.size.toULong(), ivPtr)
        }
        return iv
    }

    private fun encryptAES256GCM(
        data: ByteArray,
        key: ByteArray,
        iv: ByteArray
    ): Pair<ByteArray, ByteArray> {
        return memScoped {
            val dataPtr = data.usePinned { it.addressOf(0) }
            val keyPtr = key.usePinned { it.addressOf(0) }
            val ivPtr = iv.usePinned { it.addressOf(0) }

            // Output buffer for encrypted data
            val cipherText = ByteArray(data.size)
            val cipherTextPtr = cipherText.usePinned { it.addressOf(0) }

            // Output buffer for authentication tag
            val tag = ByteArray(tagSize)
            val tagPtr = tag.usePinned { it.addressOf(0) }

            val status = CCCryptorGCMOneshotEncrypt(
                kCCAlgorithmAES.toUInt(),
                keyPtr,
                key.size.toULong(),
                iv = ivPtr,
                ivLen = iv.size.toULong(),
                aData = null,
                aDataLen = 0u,
                dataIn = dataPtr,
                dataInLength = data.size.toULong(),
                dataOut = cipherTextPtr,
                tag = tagPtr,
                tagLength = tag.size.toULong()
            )

            val kCCSuccess = 0
            if (status != kCCSuccess) {
                throw CryptoException("AES-GCM encryption failed with status: $status", null)
            }

            Pair(cipherText, tag)
        }
    }

    private fun decryptAES256GCM(
        data: ByteArray,
        key: ByteArray,
        iv: ByteArray,
        tag: ByteArray
    ): ByteArray {

        return memScoped {
            val dataPtr = data.usePinned { it.addressOf(0) }
            val keyPtr = key.usePinned { it.addressOf(0) }
            val ivPtr = iv.usePinned { it.addressOf(0) }
            val tagPtr = tag.usePinned { it.addressOf(0) }

            // Output buffer for decrypted data
            val plainText = ByteArray(data.size)
            val plainTextPtr = plainText.usePinned { it.addressOf(0) }

            val status = CCCryptorGCMOneshotDecrypt(
                kCCAlgorithmAES.toUInt(),
                keyPtr,
                key.size.toULong(),
                iv = ivPtr,
                ivLen = iv.size.toULong(),
                aData = null, // No additional authenticated data
                aDataLen = 0u,
                dataIn = dataPtr,
                dataInLength = data.size.toULong(),
                dataOut = plainTextPtr,
                tag = tagPtr,
                tagLength = tag.size.toULong()
            )

            val kCCSuccess = 0
            if (status != kCCSuccess) {
                throw CryptoException("AES-GCM decryption failed with status: $status", null)
            }

            plainText
        }
    }

}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("CCCryptorGCMOneshotDecrypt")
external fun CCCryptorGCMOneshotDecrypt(
    algorithm: UInt,
    key: CPointer<*>?,
    keyLength: ULong,
    iv: CPointer<*>?,
    ivLen: ULong,
    aData: CPointer<*>?,
    aDataLen: ULong,
    dataIn: CPointer<*>?,
    dataInLength: ULong,
    dataOut: CPointer<*>?,
    tag: CPointer<*>?,
    tagLength: ULong
): Int

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("CCCryptorGCMOneshotEncrypt")
external fun CCCryptorGCMOneshotEncrypt(
    algorithm: UInt,
    key: CPointer<*>?,
    keyLength: ULong,
    iv: CPointer<*>?,
    ivLen: ULong,
    aData: CPointer<*>?,
    aDataLen: ULong,
    dataIn: CPointer<*>?,
    dataInLength: ULong,
    dataOut: CPointer<*>?,
    tag: CPointer<*>?,
    tagLength: ULong
): Int

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    return ByteArray(this.length.toInt()).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData {
    return this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}

private const val kCCAlgorithmAES: Int = 0