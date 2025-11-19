package com.mandor.kmp.persistance.internal

actual class Lock {
    actual fun <T> withLock(block: () -> T): T = synchronized(this) { block() }
}
