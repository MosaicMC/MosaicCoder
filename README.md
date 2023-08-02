
# MosaicCoder Documentation

## Introduction

MosaicCoder is a library designed for a configuration system based around [DFU (DataFixerUpper)](https://github.com/Mojang/DataFixerUpper). This documentation will guide you through the process of using MosaicCoder for your projects.

Before diving into this library, it is recommended to familiarize yourself with the basics of DFU. You can check out the following link for more information: [DFU Basics](https://gist.github.com/Drullkus/1bca3f2d7f048b1fe03be97c28f87910).

## Usage

### 1. Add Modrinth Repository

First, you need to add the Modrinth repository to your `build.gradle` file. This repository hosts the MosaicCoder library.

**Groovy**

```groovy
repositories {
    // ...
    maven {
        url = "https://api.modrinth.com/maven"
    }
}
```

**Kotlin**

```kotlin
repositories {
    maven { url = uri("https://api.modrinth.com/maven") }
}
```

### 2. Add MosaicCoder Dependency

Next, include the MosaicCoder dependency in your `build.gradle` file. Replace `{version}` with the latest version found [here](https://example.com):

**Groovy**
```groovy
dependencies {
    implementation "maven.modrinth:mosaiccoder:{version}"
}
```
**Kotlin**
```kotlin
dependencies {
    implementation("maven.modrinth:mosaiccoder:{version}")
}
```

### Creating Config

Creating a configuration using MosaicCoder is straightforward. By default, MosaicCoder creates JSON files for configuration, but this behavior can be customized. Refer to the library's Javadoc for more information.

Here's an example of creating your own config:
```kotlin
data class ExampleDataClass(val a: Int, val b: String)

// Define a codec for the ExampleDataClass using RecordCodecBuilder
val exampleCodec: Codec<ExampleDataClass> = RecordCodecBuilder.create { instance ->
    instance.group(
        Codec.INT.optionalFieldOf("a", 0).forGetter { it.a },
        Codec.STRING.optionalFieldOf("b", "").forGetter { it.b }
    ).apply(instance, ::ExampleDataClass)
}

// Initialize the config in your plugin
fun init(plugin: PluginContainer) {
    val example = ExampleDataClass(1, "100")
    plugin.createConfig("example.json", exampleCodec, example.convertTo())
}
``` 

### Reading Config

Reading an existing configuration is done using `PluginContainer.readConfig(...)`. Before using this function, refer to the library's Javadoc for detailed information.

Here's an example of reading your own config:
```kotlin
data class ExampleDataClass(val a: Int, val b: String)

// Define a codec for the ExampleDataClass using RecordCodecBuilder
val exampleCodec: Codec<ExampleDataClass> = RecordCodecBuilder.create { instance ->
    instance.group(
        Codec.INT.optionalFieldOf("a", 0).forGetter { it.a },
        Codec.STRING.optionalFieldOf("b", "").forGetter { it.b }
    ).apply(instance, ::ExampleDataClass)
}

// Initialize the config in your plugin
fun init(plugin: PluginContainer) {
    val readConfigResult = plugin.readConfig("example.json", exampleCodec)
    readConfigResult.result().ifPresent { println("Read the file!") }
}
``` 

### Writing Config

To modify an existing configuration, use `PluginContainer.writeConfig(...)`. Before using this function, refer to the library's Javadoc for detailed information.

Here's an example of writing to your own config:

```kotlin
data class ExampleDataClass(val a: Int, val b: String)

// Initialize the config in your plugin
fun init(plugin: PluginContainer) {
    val example = ExampleDataClass(1, "100")
    plugin.writeConfig("example.json", example.convertTo())
}
``` 

### Read or Create Config

The `PluginContainer.createOrReadConfig(...)` function allows you to create a config if it doesn't exist or read it if it does. Before using this function, refer to the library's Javadoc for detailed information.

Here's an example of creating or reading your own config:

```kotlin
data class ExampleDataClass(val a: Int, val b: String)

// Define a codec for the ExampleDataClass using RecordCodecBuilder
val exampleCodec: Codec<ExampleDataClass> = RecordCodecBuilder.create { instance ->
    instance.group(
        Codec.INT.optionalFieldOf("a", 0).forGetter { it.a },
        Codec.STRING.optionalFieldOf("b", "").forGetter { it.b }
    ).apply(instance, ::ExampleDataClass)
}

// Initialize the config in your plugin
fun init(plugin: PluginContainer) {
    val example = ExampleDataClass(1, "100")
    val readOrCreateConfigResult = plugin.createOrReadConfig("example.json", exampleCodec, example.convertTo())
    readOrCreateConfigResult.result().ifPresent { println("Created or read the file!") }
}
```
