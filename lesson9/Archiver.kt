package ru.tbank.education.school.lesson9

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun makeArchive(
    copyFromPath: File,
    copyToPath: File
) {
    ZipOutputStream(FileOutputStream(copyToPath)).use { zipOut ->
        addToArchive(copyFromPath, copyFromPath, zipOut)
    }
}

fun addToArchive(
    rootPath: File,
    currentFile: File,
    zipOut: ZipOutputStream
) {
    val extensions = listOf(".txt", ".log")

    currentFile.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            addToArchive(rootPath, file, zipOut)
        } else if (extensions.any { file.name.endsWith(it) }) {
            val newPath = rootPath.toPath().relativize(file.toPath()).toString()

            zipOut.putNextEntry(ZipEntry(newPath))
            FileInputStream(file).use { input ->
                input.copyTo(zipOut)
            }
            zipOut.closeEntry()

            println("$newPath ${file.length()} bytes")
        }
    }
}

fun main() {
    val copyFrom = File("lesson9/data")

    if (!copyFrom.exists()) {
        copyFrom.mkdirs()
        println("Папка не создана")
    } else {
        val copyTo = File("lesson9/archive.zip")
        makeArchive(copyFrom, copyTo)
    }
}