package com.mandor.kmp.persistance.internal

import platform.Foundation.NSRecursiveLock

actual class Lock {
    private val lock = NSRecursiveLock()

    actual fun <T> withLock(block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }
}
