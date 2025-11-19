package com.mandor.kmp.persistance.internal

expect class Lock() {
    fun <T> withLock(block: () -> T): T
}
