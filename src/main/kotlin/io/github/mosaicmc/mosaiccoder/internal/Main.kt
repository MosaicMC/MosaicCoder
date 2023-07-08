package io.github.mosaicmc.mosaiccoder.internal

import io.github.mosaicmc.mosaiccore.api.plugin.PluginContainer
import io.github.mosaicmc.mosaiccore.api.plugin.logger

@Suppress("UNUSED")
fun init(plugin: PluginContainer) {
    plugin.logger.info("test")
}
