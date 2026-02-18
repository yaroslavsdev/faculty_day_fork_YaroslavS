package org.example
import kotlinx.coroutines.*
import java.io.File
import java.net.URL

//Задание 11. Многопоточный загрузчик изображений
//Напишите программу, которая параллельно скачивает изображения из интернета.

//Требования:
//Использовать корутины с Dispatchers.IO
//Скачать 10 изображений с https://picsum.photos/200/300
//Сохранить в папку downloads/
//Вывести прогресс: "Downloaded 1/10", "Downloaded 2/10", ...
//В конце вывести статистику: общее время, количество успешных/неуспешных загрузок

data class DownloadStats(
    val totalTimeInMs: Long,
    val success: Int,
    val failed: Int
)
object ImageDownloader {
    fun run(urls: List<String>, outputDir: String): DownloadStats = runBlocking {
        val dir = File(outputDir)
        if (dir.exists() == false) {
            dir.mkdirs()
        }

        var success = 0
        var failed = 0
        var downloaded = 0
        val startTime = System.currentTimeMillis()

        val jobs = urls.mapIndexed { index, url ->
            async (Dispatchers.IO) {
                try {
                    val bytes = URL(url).readBytes()
                    val file = File(dir, "image_$index.png")
                    file.writeBytes(bytes)

                    synchronized(this@ImageDownloader) {
                        success++
                        downloaded++
                        println("Downloaded $downloaded/${urls.size}")
                    }
                } catch (e: Exception) {
                    synchronized(this@ImageDownloader) {
                        failed++
                        downloaded++
                        println("Download failed $downloaded/${urls.size}")
                    }
                }
            }
        }

        jobs.forEach { it.await() }

        // --- Статистика ---
        val totalTime = System.currentTimeMillis() - startTime
        println("Total time: $totalTime ms")
        println("Success: $success")
        println("Failed: $failed")

        DownloadStats(totalTime, success, failed)
    }
}

fun main() {
    val urls = mutableListOf<String>()
    for (i in 1..5) {
        urls.add("https://picsum.photos/1280/720")
    }
    ImageDownloader.run(urls, "C:\\Users\\Yaroslav\\Documents\\tbank gitclone\\faculty_day_fork_YaroslavS\\lesson11-Practice\\downloads")
}