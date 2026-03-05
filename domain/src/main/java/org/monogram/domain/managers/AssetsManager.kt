package org.monogram.domain.managers

import java.io.File
import java.io.InputStream

interface AssetsManager {
    fun getAssets(path: String): InputStream
    fun getFilesDir(): File
}