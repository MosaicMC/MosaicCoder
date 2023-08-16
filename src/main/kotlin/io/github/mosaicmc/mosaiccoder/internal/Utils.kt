/*
 * Copyright (c) 2023. JustFoxx
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
@file:JvmName("Utils")
@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package io.github.mosaicmc.mosaiccoder.internal

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mojang.serialization.DataResult
import io.github.mosaicmc.mosaiccore.api.plugin.PluginContainer
import io.github.mosaicmc.mosaiccore.api.plugin.name
import java.io.File
import net.fabricmc.loader.impl.FabricLoaderImpl

inline internal fun <T> wrapResult(wrapped: () -> DataResult<T>): DataResult<T> =
    try {
        wrapped()
    } catch (e: Exception) {
        DataResult.error { e.message }
    }

val gson: Gson = GsonBuilder().setPrettyPrinting().create()

val <T> T.asJsonObject: JsonObject
    get() = gson.toJsonTree(this).asJsonObject

/** Represents the directory where configuration files are stored. */
val PluginContainer.configDir: File
    get() = FabricLoaderImpl.INSTANCE.configDir.resolve(name).toFile()

inline internal fun <TKey, TValue> Map<TKey, *>.getExt(key: TKey): TValue? = this[key] as? TValue

inline internal fun <TKey, TValue> Map<TKey, *>.getOrDefaultExt(
    key: TKey,
    default: TValue
): TValue = (this[key] ?: default) as TValue
