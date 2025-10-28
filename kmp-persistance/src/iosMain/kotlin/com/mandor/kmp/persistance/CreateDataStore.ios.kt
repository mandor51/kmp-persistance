package com.mandor.kmp.persistance

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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