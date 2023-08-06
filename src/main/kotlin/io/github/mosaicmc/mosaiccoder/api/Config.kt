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
@file:JvmName("Config")
@file:Suppress("unused")
package io.github.mosaicmc.mosaiccoder.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import io.github.mosaicmc.mosaiccoder.internal.wrapResult
import io.github.mosaicmc.mosaiccore.api.plugin.PluginContainer
import io.github.mosaicmc.mosaiccore.api.plugin.name
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import net.fabricmc.loader.impl.FabricLoaderImpl

/** Represents the directory where configuration files are stored. */
val PluginContainer.configDir: File
    get() = FabricLoaderImpl.INSTANCE.configDir.resolve(name).toFile()

/** Gson instance used for JSON serialization and deserialization. */
val gson: Gson = GsonBuilder().setPrettyPrinting().create()

val <T : Any> T.asJsonObject: JsonObject
    get() = gson.toJsonTree(this).asJsonObject

fun PluginContainer.writeConfig(
    fileName: String,
    dataToWrite: JsonObject,
): DataResult<JsonObject> = wrapResult {
    val file = configDir.resolve(fileName)
    if (!file.exists()) {
        return@wrapResult DataResult.error { "file not found" }
    }
    FileWriter(file).use { gson.toJson(dataToWrite, it) }
    return@wrapResult DataResult.success(dataToWrite)
}

fun <T : Any> PluginContainer.writeConfig(
    fileName: String,
    dataToWrite: T,
    codec: Codec<T>,
): DataResult<T> = wrapResult {
    val file = configDir.resolve(fileName)
    if (!file.exists()) {
        return@wrapResult DataResult.error { "file not found" }
    }
    val parsed = codec.parse(JsonOps.INSTANCE, dataToWrite.asJsonObject)
    if (parsed.error().isPresent) return@wrapResult parsed
    FileWriter(file).use { gson.toJson(parsed.result().get(), it) }
    return@wrapResult parsed
}

fun <T : Any> PluginContainer.readConfig(
    fileName: String = "common.json",
    codec: Codec<T>
): DataResult<T> = wrapResult {
    val file = configDir.resolve(fileName)
    if (!file.exists()) {
        return@wrapResult DataResult.error { "file not found" }
    }

    val readFile = JsonParser.parseReader(FileReader(file)).asJsonObject
    return@wrapResult codec.parse(JsonOps.INSTANCE, readFile)
}

fun <T : Any> PluginContainer.createConfig(
    fileName: String = "common.json",
    codec: Codec<T>,
    defaultObject: JsonObject = JsonObject()
): DataResult<T> = wrapResult {
    val file = configDir.resolve(fileName)
    if (!file.exists()) {
        file.parentFile.mkdirs()
        file.createNewFile()
    } else {
        return@wrapResult DataResult.error { "file already exists" }
    }
    val parsed = codec.parse(JsonOps.INSTANCE, defaultObject)
    if (parsed.error().isPresent) return@wrapResult parsed
    FileWriter(file).use { gson.toJson(parsed.result().get(), it) }
    return@wrapResult parsed
}

fun <T : Any> PluginContainer.createOrReadConfig(
    fileName: String = "common.json",
    codec: Codec<T>,
    defaultObject: JsonObject = JsonObject(),
): DataResult<T> =
    if (configDir.resolve(fileName).exists()) readConfig(fileName, codec)
    else createConfig(fileName, codec, defaultObject)

fun <T : Any> PluginContainer.createOrReadConfig(
    fileName: String = "common.json",
    codec: Codec<T>,
    defaultObject: T,
): DataResult<T> = createOrReadConfig(fileName, codec, defaultObject.asJsonObject)
