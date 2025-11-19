package com.mandor.kmp.persistance

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import com.mandor.kmp.persistance.internal.Lock
import com.mandor.kmp.persistance.internal.getCryptoProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.KSerializer
import okio.FileSystem
import okio.Path.Companion.toPath

object KmpPersistence {
    private val cache = mutableMapOf<String, Pair<DataStore<*>, String>>()
    private val lock = Lock()

    fun <T> createSecureDataStore(
        path: () -> String,
        serializer: KSerializer<T>,
        defaultValue: T,
        scope: CoroutineScope
    ): DataStore<T> {
        val pathString = path()
        val typeName = serializer.descriptor.serialName

        return lock.withLock {
            val (dataStore, cachedTypeName) = cache.getOrPut(pathString) {
                val cryptoProvider = getCryptoProvider()
                val secureSerializer = SecureSerializer(serializer, cryptoProvider, defaultValue)
                val store = DataStoreFactory.create(
                    storage = OkioStorage(
                        fileSystem = FileSystem.SYSTEM,
                        serializer = secureSerializer,
                        producePath = { pathString.toPath() }
                    ),
                    scope = scope
                )
                store to typeName
            }

            if (cachedTypeName != typeName) {
                throw IllegalStateException(
                    "DataStore for '$pathString' is already created with type '$cachedTypeName', " +
                            "but requested type is '$typeName'. A file can only be opened with one type."
                )
            }

            @Suppress("UNCHECKED_CAST")
            dataStore as DataStore<T>
        }
    }
}
