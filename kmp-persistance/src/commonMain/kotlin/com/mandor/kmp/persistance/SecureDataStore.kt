package com.mandor.kmp.persistance

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow

class SecureDataStore<T>() : DataStore<T> {
    override suspend fun updateData(transform: suspend (T) -> T): T {
        TODO("Not yet implemented")
    }

    override val data: Flow<T>
}