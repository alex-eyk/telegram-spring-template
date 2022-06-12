package com.alex.eyk.telegram.core.util

import com.ximand.properties.JarUtils
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.io.File
import java.io.FileNotFoundException

object ResourceUtils {

    fun findDictionaries(
        dictionariesRelativePath: String
    ): Iterable<File> = try {
        scanResourceDirectory(dictionariesRelativePath)
    } catch (e: FileNotFoundException) {
        scanJarDirectory(dictionariesRelativePath)
    }


    private fun scanResourceDirectory(
        dictionariesRelativePath: String
    ): Iterable<File> {
        val resolver = PathMatchingResourcePatternResolver()
        val directory: File = resolver.getResource(dictionariesRelativePath).file
        val innerFiles = directory.listFiles()
            ?: throw IllegalStateException("Directory not contains files")
        return innerFiles.toHashSet()
    }

    private fun scanJarDirectory(
        dictionariesRelativePath: String
    ): Iterable<File> {
        val dictionariesAbsolutePath = JarUtils
            .getFileFromJarDirectoryPath(dictionariesRelativePath, javaClass)
        val file = File(dictionariesAbsolutePath)
        val innerFiles = file.listFiles()
            ?: throw IllegalStateException("Directory: $dictionariesAbsolutePath not contains files")
        return innerFiles.toHashSet()
    }
}
