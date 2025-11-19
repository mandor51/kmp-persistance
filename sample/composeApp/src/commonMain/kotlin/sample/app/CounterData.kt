package sample.app

import kotlinx.serialization.Serializable

@Serializable
data class CounterData(val value: Int = 0)
