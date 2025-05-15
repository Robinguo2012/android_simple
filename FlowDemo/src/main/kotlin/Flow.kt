package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


fun interface FlowCollector<T> {
    suspend fun emit(value: T)
}

fun interface Flow<T> {
    suspend fun collect(collector: FlowCollector<T>)
}

fun <T, R> Flow<T>.map(mapper: suspend (T) -> R): Flow<R> {
    return Flow { downstream ->
        this@map.collect {
            downstream.emit(mapper(it))
        }
    }
}

fun <T> Flow<T>.filter(predicate: suspend (T) -> Boolean): Flow<T> {
    return Flow { downstream ->
        this@filter.collect {
            if (predicate(it)) {
                downstream.emit(it)
            }
        }
    }
}

fun <T> Flow<T>.flowOnSimulate(dispatcherName: String): Flow<T> {
    return Flow { downstream ->
        this@flowOnSimulate.collect { value ->
            println("Running upstream on dispatcher: $dispatcherName")
            downstream.emit(value)
        }
    }
}

fun <T> Flow<T>.catch(handler: (e: Throwable) -> Unit): Flow<T> {
    return Flow { downstream ->
        try {
            this@catch.collect { value ->
                downstream.emit(value)
            }
        }catch (t:Throwable){
            handler(t)
        }
    }
}

fun <T, R> Flow<T>.flatMapConcat(transform: suspend (T) -> Flow<R>): Flow<R> {
    return Flow { downstream ->
        this@flatMapConcat.collect { value ->
            val innerFlow = transform(value)
            innerFlow.collect { value ->
                downstream.emit(value)
            }
        }
    }
}

//fun <A, B, R> combine(
//    flowA: Flow<A>,
//    flowB: Flow<B>,
//    transform: suspend (A, B) -> R
//): Flow<R> {
//    return Flow { downstream ->
//        var latestA: A? = null
//        var latestB: B? = null
//
//        val mutex = Mutex()
//
//        val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Default)
//        scope.launch {
//            flowA.collect { a ->
//                mutex.lock()
//                latestA = a
//                val b = latestB
//                mutex.unlock()
//                if (b != null) {
//                    downstream.emit(transform(a, b))
//                }
//            }
//        }
//
//        scope.launch {
//            flowB.collect { b->
//                mutex.lock()
//                latestB= b
//                val a = latestA
//                mutex.unlock()
//                if (a != null) {
//                    downstream.emit(transform(a, b))
//                }
//            }
//        }
//    }
//}

fun <A, B, R> combine(
    flowA: Flow<A>,
    flowB: Flow<B>,
    transform: suspend (A, B) -> R
): Flow<R> {
    return Flow { downstream ->
        var latestA: A? = null
        var latestB: B? = null
        val mutex = kotlinx.coroutines.sync.Mutex()
        val scope = CoroutineScope(Dispatchers.Default)
        val job = Job()

        val emitIfBothReady: suspend () -> Unit = {
            val a = latestA
            val b = latestB
            if (a != null && b != null) {
                downstream.emit(transform(a, b))
            }
        }

        val jobA = scope.launch(job) {
            flowA.collect { a ->
                mutex.withLock {
                    latestA = a
                    emitIfBothReady()
                }
            }
        }

        val jobB = scope.launch(job) {
            flowB.collect { b ->
                mutex.withLock {
                    latestB = b
                    emitIfBothReady()
                }
            }
        }

        jobA.join()
        jobB.join()
    }
}


fun <T> myFlow(block: suspend FlowCollector<T>.()->Unit): Flow<T> {
    return Flow { collector ->
        collector.block()
    }
}



