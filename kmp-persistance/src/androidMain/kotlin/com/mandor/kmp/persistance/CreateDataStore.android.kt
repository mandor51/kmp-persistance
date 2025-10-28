package com.mandor.kmp.persistance

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun createDataStore(
    context: Context,
    fileName: String = DATA_STORE_DEFAULT_FILE_NAME,
    encrypted: Boolean = false,
) : DataStore<Preferences> {
    return createDataStore(
        encrypted = encrypted,
        producePath = { context.filesDir.resolve(fileName).absolutePath }
    )
}