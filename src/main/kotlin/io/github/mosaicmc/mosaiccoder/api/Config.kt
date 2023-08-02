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

package io.github.mosaicmc.mosaiccoder.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import io.github.mosaicmc.mosaiccore.api.plugin.PluginContainer
import io.github.mosaicmc.mosaiccore.api.plugin.name
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import net.fabricmc.loader.impl.FabricLoaderImpl

/** Represents the directory where configuration files are stored. */
val PluginContainer.configDir: File
    get() = FabricLoaderImpl.INSTANCE.configDir.resolve(name).toFile()

/** Gson instance used for JSON serialization and deserialization. */
val gson: Gson = GsonBuilder().setPrettyPrinting().create()

/**
 * Converts an object of type T to a JSON object using Gson serialization.
 *
 * @return The JSON representation of the object.
 */
fun <T : Any> T.convertTo(): JsonObject = gson.toJsonTree(this).asJsonObject

/**
 * Reads and parses a configuration file with the specified [fileName] and [codec].
 *
 * @param fileName The name of the configuration file to be read. Default is "common.json".
 * @param codec The Codec instance used to parse the configuration.
 * @return A [DataResult] containing the parsed configuration data.
 */
fun <T : Any> PluginContainer.readConfig(
    fileName: String = "common.json",
    codec: Codec<T>
): DataResult<T> =
    readConfig(fileName, codec, JsonOps.INSTANCE) {
        JsonParser.parseReader(FileReader(it)).asJsonObject
    }

/**
 * Creates a new configuration file with the specified [fileName] and [codec], using the provided
 * [defaultObject].
 *
 * @param fileName The name of the configuration file to be created. Default is "common.json".
 * @param codec The Codec instance used to encode the configuration.
 * @param defaultObject The default configuration data to be used if the file does not exist.
 * @return A [DataResult] representing the success or failure of the creation process.
 */
fun <T : Any> PluginContainer.createConfig(
    fileName: String = "common.json",
    codec: Codec<T>,
    defaultObject: JsonObject = JsonObject()
): DataResult<T> =
    createConfig(fileName, codec, JsonOps.INSTANCE, defaultObject) { file, either ->
        FileWriter(file).use { gson.toJson(either.left().get(), it) }
    }

/**
 * Reads an existing configuration file or creates a new one if it doesn't exist. Uses the specified
 * [fileName], [codec], and [defaultObject].
 *
 * @param fileName The name of the configuration file to be read or created. Default is
 *   "common.json".
 * @param codec The Codec instance used to parse the configuration.
 * @param defaultObject The default configuration data to be used if the file doesn't exist.
 * @return A [DataResult] containing the parsed or created configuration data.
 */
fun <T : Any> PluginContainer.createOrReadConfig(
    fileName: String = "common.json",
    codec: Codec<T>,
    defaultObject: JsonObject = JsonObject()
): DataResult<T> =
    createOrReadConfig(
        fileName,
        codec,
        JsonOps.INSTANCE,
        defaultObject,
        { file, either -> FileWriter(file).use { gson.toJson(either.left().get(), it) } },
        { JsonParser.parseReader(FileReader(it)).asJsonObject }
    )

/**
 * Writes configuration data to a specified file using a json file writer.
 *
 * @param fileName The name of the configuration file to be written.
 * @param data The data to be written to the file.
 * @return A [DataResult] representing the success or failure of the write operation.
 */
fun PluginContainer.writeConfig(fileName: String, data: JsonObject): DataResult<JsonObject> =
    this.writeConfig(fileName, data) { file -> FileWriter(file).use { gson.toJson(data, it) } }

/**
 * Writes configuration data to a specified file using the provided [fileWriter].
 *
 * @param fileName The name of the configuration file to be written.
 * @param dataToWrite The data to be written to the file.
 * @param fileWriter A function that writes the configuration data to the file.
 * @return A [DataResult] representing the success or failure of the write operation.
 */
fun PluginContainer.writeConfig(
    fileName: String,
    dataToWrite: JsonObject,
    fileWriter: (File) -> Unit
): DataResult<JsonObject> {
    val file = configDir.resolve(fileName)
    if (!file.exists()) {
        return DataResult.error { "File not found" }
    }

    return try {
        fileWriter(file)
        DataResult.success(dataToWrite)
    } catch (e: Exception) {
        DataResult.error { "Failed to write file: ${e.message}" }
    }
}

/**
 * Reads and parses a configuration file with the specified [fileName], [codec], and [ops].
 *
 * @param fileName The name of the configuration file to be read. Default is "common.json".
 * @param codec The Codec instance used to parse the configuration.
 * @param ops The DynamicOps instance used to read the configuration file.
 * @param fileParser A function that reads the configuration file and returns the configuration data
 *   as [R].
 * @return A [DataResult] containing the parsed configuration data.
 */
fun <T : Any, R : Any> PluginContainer.readConfig(
    fileName: String = "common.json",
    codec: Codec<T>,
    ops: DynamicOps<R>,
    fileParser: (File) -> R
): DataResult<T> {
    val file = configDir.resolve(fileName)
    if (!file.exists()) {
        return DataResult.error { "File not found" }
    }
    return try {
        val readFile = fileParser(file)
        codec.parse(ops, readFile)
    } catch (e: Exception) {
        DataResult.error { "Invalid JSON: ${e.message}" }
    }
}

/**
 * Creates a new configuration file with the specified [fileName], [codec], [ops], and
 * [defaultObject].
 *
 * @param fileName The name of the configuration file to be created. Default is "common.json".
 * @param codec The Codec instance used to encode the configuration.
 * @param ops The DynamicOps instance used to write the configuration file.
 * @param defaultObject The default configuration data to be used if the file doesn't exist.
 * @param fileWriter A function that writes the configuration data to the file.
 * @return A [DataResult] representing the success or failure of the creation process.
 */
fun <T : Any, R : Any> PluginContainer.createConfig(
    fileName: String = "common.json",
    codec: Codec<T>,
    ops: DynamicOps<R>,
    defaultObject: R,
    fileWriter: (File, Either<T, DataResult.PartialResult<T>>) -> Unit
): DataResult<T> {
    val file = configDir.resolve(fileName)
    if (!file.exists()) {
        try {
            file.parentFile.mkdirs()
            file.createNewFile()
        } catch (e: IOException) {
            return DataResult.error { "Failed to create file: ${e.message}" }
        }
    } else {
        return DataResult.error { "File already exists" }
    }
    val parsed = codec.parse(ops, defaultObject)
    if (parsed.error().isPresent) return parsed
    try {
        fileWriter(file, parsed.get())
    } catch (e: IOException) {
        return DataResult.error { "Failed to write file: ${e.message}" }
    }
    return parsed
}

/**
 * Reads an existing configuration file or creates a new one if it doesn't exist. Uses the specified
 * [fileName], [codec], [ops], and [emptyObject].
 *
 * @param fileName The name of the configuration file to be read or created. Default is
 *   "common.json".
 * @param codec The Codec instance used to parse the configuration.
 * @param ops The DynamicOps instance used to read/write the configuration file.
 * @param emptyObject The empty configuration data to be used if the file doesn't exist.
 * @param fileWriter A function that writes the configuration data to the file.
 * @param fileParser A function that reads the configuration file and returns the configuration data
 *   as [R].
 * @return A [DataResult] containing the parsed or created configuration data.
 */
fun <T : Any, R : Any> PluginContainer.createOrReadConfig(
    fileName: String = "common.json",
    codec: Codec<T>,
    ops: DynamicOps<R>,
    emptyObject: R,
    fileWriter: (File, Either<T, DataResult.PartialResult<T>>) -> Unit,
    fileParser: (File) -> R
): DataResult<T> =
    if (configDir.resolve(fileName).exists()) readConfig(fileName, codec, ops, fileParser)
    else createConfig(fileName, codec, ops, emptyObject, fileWriter)
