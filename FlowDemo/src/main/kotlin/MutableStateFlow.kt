package org.example

import kotlinx.coroutines.flow.flow

class MutableStateFlow<T>(initial: T): Flow<T> {

    var collectors = mutableListOf<FlowCollector<T>>()
    var value: T = initial

    suspend fun emit(newValue: T) {
        value = newValue
        collectors.forEach { it.emit(newValue) }
    }

    fun value(): T = value

    override suspend fun collect(collector: FlowCollector<T>) {
        collectors.add(collector)
        emit(value)
    }

}