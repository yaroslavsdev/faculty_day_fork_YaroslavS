package org.example

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlinx.coroutines.*
import java.io.File

//Семинар: Многопоточность и корутины в Kotlin
//Часть 1. Потоки (Thread)
//Задание 1. Создание потоков
//Создайте 3 потока с именами "Thread-A", "Thread-B", "Thread-C". Каждый поток должен вывести своё имя 5 раз с задержкой 500мс.

object CreateThreads {
    fun run(): List<Thread> {
        val threads = listOf("Thread-A", "Thread-B", "Thread-C").map { name ->
            Thread {
                repeat(5) {
                    println("${Thread.currentThread().name}")
                    Thread.sleep(500)
                }
            }.apply { this.name = name }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        return threads
    }
}


//Задание 2. Race condition
//Создайте переменную counter = 0. Запустите 10 потоков, каждый из которых увеличивает counter на 1000. Выведите финальное значение и объясните результат.

object RaceCondition {
    private var counter = 0
    fun run(): Int {
        counter = 0

        val threads = List(10) {
            Thread {
                repeat(1000) {
                    counter++
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        println("Ответ = $counter")
        return counter
    }
}

//Задание 3. Synchronized
//Исправьте задание 2 с помощью @Synchronized или synchronized {} блока, чтобы результат всегда был 10000.

object SynchronizedCounter {
    private var counter = 0
    private val lock = Any()

    fun run(): Int {
        counter = 0

        val threads = List(10) {
            Thread {
                repeat(1000) {
                    synchronized(lock) {
                        counter++
                    }
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        return counter
    }
}




//Задание 4. Deadlock
//Создайте пример deadlock с двумя ресурсами и двумя потоками. Затем исправьте его.

object Deadlock {
    object Deadlock {
        fun runDeadlock() {
            val lock1 = Any()
            val lock2 = Any()

            val t1 = Thread {
                synchronized(lock1) {
                    Thread.sleep(100)
                    synchronized(lock2) {
                        println("Thread 1")
                    }
                }
            }

            val t2 = Thread {
                synchronized(lock2) {
                    Thread.sleep(100)
                    synchronized(lock1) {
                        println("Thread 2")
                    }
                }
            }

            t1.start()
            t2.start()
        }
    }
}

//Часть 2. Executor Framework
//Задание 5. ExecutorService
//Используя Executors.newFixedThreadPool(4), выполните 20 задач. Каждая задача выводит свой номер и имя потока, затем спит 200мс.

object ExecutorServiceExample {
    fun run(): List<String> {
        val executor = Executors.newFixedThreadPool(4)
        val results = mutableListOf<String>()

        repeat(20) { i ->
            executor.submit {
                val m = "Task $i executed: ${Thread.currentThread().name}"
                println(m)
                synchronized(results) {
                    results.add(m)
                }
                Thread.sleep(200)
            }
        }

        executor.shutdown()
        return results
    }
}


//Задание 6. Future
//Используя ExecutorService и Callable, параллельно вычислите факториалы чисел от 1 до 10. Соберите результаты через Future.get().

object FutureFactorial {

    private fun factorial(n: Int): Long {
        var result: Long = 1
        for (i in 1..n) {
            result *= i
        }
        return result
    }

    fun run(): Map<Int, Long> {
        val executor = Executors.newFixedThreadPool(4)
        val futures = (1..10).associateWith { n ->    // делает мапу
            executor.submit(Callable { factorial(n) })
        }

        val results = futures.mapValues { it.value.get() }

        executor.shutdown()
        return results
    }
}



//Часть 3. Корутины
//Задание 7. Первая корутина
//Используя runBlocking и launch, запустите 3 корутины, каждая из которых выводит своё имя 5 раз с delay(500).

object CoroutineLaunch {
    fun run(): List<String> = runBlocking {
        val results = mutableListOf<String>()

        val jobs = List(3) { index ->
            launch {
                repeat(5) {
                    val text = "Корутина $index"
                    println(text)
                    synchronized(results) {
                        results.add(text)
                    }
                    delay(500)
                }
            }
        }

        jobs.forEach { it.join() }
        results
    }
}


//Задание 8. async/await
//Используя async, параллельно вычислите сумму чисел от 1 до 1_000_000, разбив на 4 части. Соберите результаты через await().

object AsyncAwait {
    fun run(): Long = runBlocking {
        val blockSize = 1_000_000L / 4

        val splited = List(4) { index ->
            async {
                val start = index * blockSize + 1
                val end = if (index == 3) 1_000_000 else (index + 1) * blockSize
                (start..end).sum()
            }
        }

        splited.sumOf { it.await() }
    }
}


//Задание 9. Structured concurrency
//Создайте корутину, которая запускает 5 дочерних корутин. Если одна из них падает с исключением, все остальные должны отмениться.

object StructuredConcurrency {
    fun run(failingCoroutineIndex: Int): Int = runBlocking {
        try {
            coroutineScope {
                repeat(5) { index ->
                    launch {
                        if (index == failingCoroutineIndex) {
                            throw RuntimeException("Фатальная ошибка в $index")
                        }
                        delay(1000)
                    }
                }
            }
            1
        } catch (e: Exception) {
            -1
        }
    }
}



//Задание 10. withContext
//Используя withContext(Dispatchers.IO), прочитайте содержимое 3 файлов параллельно и объедините результаты.

object WithContextIO {
    fun run(filePaths: List<String>): Map<String, String> = runBlocking {
        filePaths.associateWith { path ->
            async {
                withContext(Dispatchers.IO) {
                    File(path).readText()
                }
            }
        }.mapValues { it.value.await() }
    }
}