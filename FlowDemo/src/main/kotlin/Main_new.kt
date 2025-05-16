package org.example

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

//fun main() = runBlocking {
//
//    val flow = myFlow {
//        // create a lambda
//        emit(1)
//        emit(2)
//        emit(3)
//        emit(4)
//    }
//
////    flow.collect { value -> println(value) }
//
//    flow.filter { it%2 == 0 }
//        .map { it*it }
//        .collect {
//            println(it)
//        }
//}

//suspend fun main() = coroutineScope {
//    val flow = myFlow<Int> {
//        emit(1)
//        emit(2)
//        emit(3)
//        emit(4 / 0) // 故意触发异常
//    }
//
//    flow
//        .flowOnSimulate("IO")
//        .map { it * 2 }
//        .catch { e -> println("Caught exception: $e") }
//        .collect { println("Collected: $it") }
//
//
//}

suspend fun main() = coroutineScope {
//    val flow = myFlow<Int> {
//        emit(1)
//        emit(2)
//        emit(3)
//    }
//
//    flow
//        .flowOnSimulate("IO")
//        .map { it * 2 }
//        .flatMapConcat {
//            return@flatMapConcat myFlow {
//                emit("$it A")
//                emit("$it B")
//            }
//        }
//        .collect { println("Collected: $it") }

    val flow1 = myFlow<Int> {
        emit(1)
        emit(2)
    }

    val flow2 = myFlow<String> {
        emit("B")
    }

    combine(flow1, flow2) { a, b -> "$a  " + b }.collect {
        println("Combined Collected: $it")
    }

    val stateFlow = MutableStateFlow(0)
    stateFlow.emit(1)
    stateFlow.collect { println(" StateFlow Collected: 1 $it") } // 立刻收到 1

    stateFlow.emit(2)

    stateFlow.collect { println(" StateFlow Collected: 2 $it") }
}