package com.mandor.kmp.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mandor.kmp.persistance.DATA_STORE_DEFAULT_FILE_NAME
import com.mandor.kmp.persistance.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun createDataStore(
    fileName: String = DATA_STORE_DEFAULT_FILE_NAME,
    encrypted: Boolean = false
): DataStore<Preferences> {
    return createDataStore(
        encrypted = encrypted,
        producePath = {
            val directory = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            requireNotNull(directory).path + "/$fileName"
        }
    )
}