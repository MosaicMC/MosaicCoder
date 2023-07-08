package io.github.mosaicmc.example.internal

import org.slf4j.LoggerFactory

internal val logger = LoggerFactory.getLogger("example")

@Suppress("UNUSED")
fun init() {
    logger.info("Hello!")
}
