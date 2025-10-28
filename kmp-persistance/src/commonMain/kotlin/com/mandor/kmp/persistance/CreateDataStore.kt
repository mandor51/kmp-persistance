package com.mandor.kmp.persistance

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

fun createDataStore(
    encrypted: Boolean = false,
    producePath: () -> String,
): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )
}

const val DATA_STORE_DEFAULT_FILE_NAME = "prefs.preferences_pb"