@file:JvmName("Test")
package io.github.mosaicmc.mosaiccoder.internal

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.mosaicmc.mosaiccoder.api.*
import io.github.mosaicmc.mosaiccore.api.plugin.PluginContainer
import io.github.mosaicmc.mosaiccore.api.plugin.logger
import kotlin.reflect.KFunction
import org.slf4j.Logger

internal data class TestJson(val a: Int, val b: String)

internal val testJson = TestJson(1, "a")
internal val convertedTest = testJson.asJsonObject
internal val testCoded: Codec<TestJson> =
    RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.INT.optionalFieldOf("a", 0).forGetter { it.a },
                Codec.STRING.optionalFieldOf("b", "").forGetter { it.b }
            )
            .apply(instance, ::TestJson)
    }

internal fun Logger.printResult(result: KFunction<DataResult<*>>) {
    val name = result.name
    result.call()
        .error()
        .ifPresentOrElse(
            { error("Failed to test `$name`: ${it.message()}") },
            { info("Successfully test `$name`") }
        )
}

internal fun PluginContainer.`create test config`(): DataResult<TestJson> =
    createConfig("test.json", testCoded, convertedTest)

internal fun PluginContainer.`read test config`(): DataResult<TestJson> =
    readConfig("test.json", testCoded)

internal fun PluginContainer.`create or read test config`(): DataResult<TestJson> =
    createOrReadConfig("test.json", testCoded, convertedTest)

internal fun PluginContainer.`write test config`(): DataResult<JsonObject> =
    writeConfig("test.json", convertedTest)

internal fun PluginContainer.test() {
    logger.printResult(::`read test config`)
    logger.printResult(::`create or read test config`)
    logger.printResult(::`create test config`)
    logger.printResult(::`write test config`)
}
