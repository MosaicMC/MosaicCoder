package io.github.mosaicmc.mosaiccoder.api

import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import io.github.mosaicmc.mosaiccoder.internal.wrapResult
import io.github.mosaicmc.mosaiccore.api.plugin.PluginContainer
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * A utility class for managing and interacting with plugin configuration files.
 *
 * @param T The type of data being stored in the configuration.
 * @property file The file associated with the configuration.
 * @property codec The codec used to encode and decode configuration data.
 * @property default The default configuration data.
 */
class PluginConfig<T : Any>
private constructor(private val file: File, private val codec: Codec<T>, private var default: T) {

    private var data: T? = null

    /**
     * Reloads the configuration data from the file.
     *
     * @return A [DataResult] containing the reloaded configuration data or an error.
     */
    fun reload(): DataResult<T> = wrapResult { forceRead() }

    /**
     * Writes new configuration data to the file.
     *
     * @param newData The new configuration data to be written.
     * @return A [DataResult] indicating success or an error during the write operation.
     */
    fun write(newData: T): DataResult<T> = wrapResult {
        if (!file.exists()) {
            val createdData = create()
            if (createdData.error().isPresent) return@wrapResult createdData
        }

        val parsed = codec.parse(JsonOps.INSTANCE, newData.asJsonObject)
        if (parsed.error().isPresent) return@wrapResult parsed

        FileWriter(file).use { gson.toJson(parsed.result().get(), it) }
        data = newData
        parsed
    }

    /**
     * Retrieves the configuration data. If the data is not already loaded, it is loaded from the
     * file.
     *
     * @return A [DataResult] containing the configuration data or an error.
     */
    fun getData(): DataResult<T> = wrapResult {
        if (data == null) {
            val forceReadData = forceRead()
            if (forceReadData.error().isPresent) return@wrapResult forceReadData
            data = forceRead().result().get()
        }
        DataResult.success(data!!)
    }

    private fun forceRead(): DataResult<T> = wrapResult {
        if (!file.exists()) onFileNotExists()

        val readFile = JsonParser.parseReader(FileReader(file)).asJsonObject
        val parsedData = codec.parse(JsonOps.INSTANCE, readFile)

        if (parsedData.error().isPresent) return@wrapResult parsedData

        data = parsedData.result().get()
        parsedData
    }

    private fun create(): DataResult<T> = wrapResult {
        file.parentFile.mkdirs()
        file.createNewFile()

        val parsed = codec.parse(JsonOps.INSTANCE, default.asJsonObject)

        if (parsed.error().isPresent) return@wrapResult parsed

        FileWriter(file).use { gson.toJson(parsed.result().get(), it) }
        parsed
    }

    private fun onFileNotExists(): DataResult<T> = wrapResult {
        val createdData = create()
        if (createdData.error().isPresent) return@wrapResult createdData
        data = createdData.result().get()
        createdData
    }

    companion object {
        private val configs = mutableMapOf<File, PluginConfig<*>>()

        /**
         * Retrieves or creates a configuration instance for a plugin.
         *
         * @param fileName The name of the configuration file.
         * @param codec The codec used to encode and decode configuration data.
         * @param default The default configuration data.
         * @return A [PluginConfig] instance for the specified plugin and configuration file.
         */
        fun <T : Any> PluginContainer.getConfig(
            fileName: String,
            codec: Codec<T>,
            default: T
        ): PluginConfig<T> {
            val file = configDir.resolve(fileName)

            return configs.getOrDefault(file, PluginConfig(file, codec, default)) as PluginConfig<T>
        }
    }
}
