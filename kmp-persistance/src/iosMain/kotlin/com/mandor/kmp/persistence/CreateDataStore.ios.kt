package com.mandor.kmp.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mandor.kmp.persistance.DATA_STORE_FILE_NAME
import com.mandor.kmp.persistance.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun createDataStore(): DataStore<Preferences> {
    return createDataStore{
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        requireNotNull(directory?.path).plus("/$DATA_STORE_FILE_NAME")
    }
}