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
@file:JvmName("Test")

package io.github.mosaicmc.mosaiccoder.internal

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.mosaicmc.mosaiccoder.api.PluginConfig.Companion.getConfig
import io.github.mosaicmc.mosaiccore.api.plugin.PluginContainer
import io.github.mosaicmc.mosaiccore.api.plugin.logger
import kotlin.reflect.KFunction
import org.slf4j.Logger

internal data class TestJson(val a: Int, val b: String)

internal val testJson = TestJson(1, "a")
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
    result
        .call()
        .error()
        .ifPresentOrElse(
            { error("Failed to test `$name`: ${it.message()}") },
            { info("Successfully test `$name`: ${result.call().result().get()}") }
        )
}

internal fun PluginContainer.`get test config`(): DataResult<TestJson> =
    getConfig("test.json", testCoded, testJson).getData()

internal fun PluginContainer.`reload test config`(): DataResult<TestJson> =
    getConfig("test.json", testCoded, testJson).reload()

internal fun PluginContainer.`write test config`(): DataResult<TestJson> =
    getConfig("test.json", testCoded, testJson).write(TestJson(2, "z"))

internal fun PluginContainer.test() {
    logger.printResult(::`get test config`)
    logger.printResult(::`reload test config`)
    logger.printResult(::`write test config`)
}
