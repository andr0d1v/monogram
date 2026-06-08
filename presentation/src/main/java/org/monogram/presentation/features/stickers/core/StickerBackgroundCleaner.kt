package org.monogram.presentation.features.stickers.core

import android.graphics.Bitmap
import java.io.Closeable

object StickerBackgroundCleaner {
    enum class Mode(val nativeValue: Int) {
        BlackAndWhite(0),
        BlackOnly(1)
    }

    enum class Result {
        NoBackground,
        Cleaned
    }

    fun removeBlackEdgeBackground(
        bitmap: Bitmap,
        mode: Mode = Mode.BlackAndWhite
    ): Result {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
            return Result.NoBackground
        }
        return Session(bitmap.width, bitmap.height).use { session ->
            session.removeBlackEdgeBackground(bitmap, mode)
        }
    }

    class Session(
        private val width: Int,
        private val height: Int
    ) : Closeable {
        private var nativeHandle = createSession(width, height)

        fun removeBlackEdgeBackground(
            bitmap: Bitmap,
            mode: Mode = Mode.BlackAndWhite
        ): Result {
            val handle = nativeHandle
            if (handle == 0L || bitmap.isRecycled || bitmap.width != width || bitmap.height != height) {
                return Result.NoBackground
            }

            return if (cleanNative(handle, bitmap, mode.nativeValue) == NATIVE_CLEANED) {
                Result.Cleaned
            } else {
                Result.NoBackground
            }
        }

        override fun close() {
            val handle = nativeHandle
            if (handle != 0L) {
                nativeHandle = 0L
                destroySession(handle)
            }
        }
    }

    private const val NATIVE_CLEANED = 1

    init {
        System.loadLibrary("native-lib")
    }

    private external fun createSession(width: Int, height: Int): Long
    private external fun cleanNative(handle: Long, bitmap: Bitmap, mode: Int): Int
    private external fun destroySession(handle: Long)
}
