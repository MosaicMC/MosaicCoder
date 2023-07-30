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

package io.github.mosaicmc.mosaiccoder.internal

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.mosaicmc.mosaiccoder.api.convertToJsonObject
import io.github.mosaicmc.mosaiccoder.api.createConfig
import io.github.mosaicmc.mosaiccoder.api.createOrReadConfig
import io.github.mosaicmc.mosaiccoder.api.readConfig
import io.github.mosaicmc.mosaiccore.api.plugin.PluginContainer
import io.github.mosaicmc.mosaiccore.api.plugin.logger

internal const val TEST = true

@Suppress("UNUSED")
fun init(plugin: PluginContainer) {
    if (TEST) test(plugin)
}

internal fun test(plugin: PluginContainer) {
    data class TestJson(val a: Int, val b: String)
    val codec: Codec<TestJson> =
        RecordCodecBuilder.create { instance ->
            instance
                .group(
                    Codec.INT.optionalFieldOf("a", 0).forGetter { it.a },
                    Codec.STRING.optionalFieldOf("b", "").forGetter { it.b }
                )
                .apply(instance, ::TestJson)
        }
    val converted = TestJson(1, "a").convertToJsonObject()
    val createConfig = plugin.createConfig("common.json", codec, converted)
    if (createConfig.error().isPresent) {
        plugin.logger.error(createConfig.error().get().message())
    } else {
        plugin.logger.info("Successfully created common.json")
    }
    val readConfig = plugin.readConfig("common.json", codec)
    if (readConfig.error().isPresent) {
        plugin.logger.error(readConfig.error().get().message())
    } else {
        plugin.logger.info("Successfully read common.json")
    }
    val createOrReadConfig = plugin.createOrReadConfig("common.json", codec, converted)
    if (createOrReadConfig.error().isPresent) {
        plugin.logger.error(createOrReadConfig.error().get().message())
    } else {
        plugin.logger.info("Successfully created or read common.json")
    }
}
