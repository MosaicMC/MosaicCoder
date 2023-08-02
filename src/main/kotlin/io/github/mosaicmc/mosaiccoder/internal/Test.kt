package io.github.mosaicmc.mosaiccoder.internal

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.mosaicmc.mosaiccoder.api.*
import io.github.mosaicmc.mosaiccore.api.plugin.PluginContainer
import io.github.mosaicmc.mosaiccore.api.plugin.logger
import org.slf4j.Logger

internal data class TestJson(val a: Int, val b: String)

internal val testJson = TestJson(1, "a")
internal val convertedTest = testJson.convertTo()
internal val testCoded: Codec<TestJson> =
    RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.INT.optionalFieldOf("a", 0).forGetter { it.a },
                Codec.STRING.optionalFieldOf("b", "").forGetter { it.b }
            )
            .apply(instance, ::TestJson)
    }

internal fun Logger.printResult(result: DataResult<TestJson>, action: String) =
    result
        .error()
        .ifPresentOrElse(
            { error("Failed to $action: ${it.message()}") },
            { info("Successfully $action") }
        )

internal fun PluginContainer.`create test config`(): DataResult<TestJson> =
    this.createConfig("test.json", testCoded, convertedTest)

internal fun PluginContainer.`read test config`(): DataResult<TestJson> =
    this.readConfig("test.json", testCoded)

internal fun PluginContainer.`create or read test config`(): DataResult<TestJson> =
    this.createOrReadConfig("test.json", testCoded, convertedTest)

internal fun PluginContainer.`write test config`(): DataResult<TestJson> =
    this.writeConfig("test.json", TestJson(2, "b"))

internal fun PluginContainer.test() {
    val test1 = `read test config`()
    val test2 = `create or read test config`()
    val test3 = `create test config`()
    val test4 = `write test config`()
    logger.printResult(test1, "read test config")
    logger.printResult(test2, "create or read test config")
    logger.printResult(test3, "create test config")
    logger.printResult(test4, "write test config")
}
