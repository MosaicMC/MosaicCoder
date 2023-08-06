@file:JvmName("Utils")
package io.github.mosaicmc.mosaiccoder.internal

import com.mojang.serialization.DataResult

internal fun <T : Any> wrapResult(wrapped: () -> DataResult<T>): DataResult<T> =
    try {
        wrapped()
    } catch (e: Exception) {
        DataResult.error { e.message }
    }
