[![Build Status](https://travis-ci.org/Double-O-Seven/kamp-server-starter.svg?branch=master)](https://travis-ci.org/Double-O-Seven/kamp-server-starter)
 [![Gradle Plugins Release](https://img.shields.io/github/tag/Double-O-Seven/kamp-server-starter.svg)](https://plugins.gradle.org/plugin/ch.leadrian.samp.kamp.kamp-server-starter)

# Kamp Server Starter Gradle Plugin

A simple Gradle plugin to configure and start a SA-MP server running the Kamp plugin.

The plugin provides 2 relevant tasks:

  * `configureServer`: Fully configures a runnable SA-MP server by performing the following steps:
    * Download the SA-MP server package from [www.sa-mp.com](http://www.sa-mp.com)
    * Adding all required plugin binaries (SAMPGDK and Kamp)
    * If configured, add additional third-party native plugins (ColAndreas for example)
    * Write `server.cfg` based on the configuration given by the `serverStarter` Gradle extension
    * Set up the Kamp directory structure
    * Copy all runtime classpath dependencies of the gamemode into server's directory
    * Configure `config.properties`
    * Configure `jvmopts.txt`
  * `startServer`: Start a SA-MP server with the gamemode project, depends on `configureServer`
 
The plugin also provides the `serverStarter` extension.

A minimal extension configuration should look like this:

```kotlin
plugins {
    id("ch.leadrian.samp.kamp.kamp-server-starter") version "1.0.0-rc4"
}

serverStarter {
    gameModeClassName = "ch.leadrian.samp.sadm.SanAndreasDeathmatchGameMode"
}
```

However, it is recommended to configure at least the following options:
```kotlin
plugins {
    id("ch.leadrian.samp.kamp.kamp-server-starter") version "1.0.0-rc4"
}

serverStarter {
    gameModeClassName = "ch.leadrian.samp.sadm.SanAndreasDeathmatchGameMode"
    // Careful! Do not configure the RCON password here if you want to configure your productive server
    rconPassword = "test1234"
    jvmOption("-Xmx1G")
}
```

A complete configuration will look this:
```kotlin
plugins {
    id("ch.leadrian.samp.kamp.kamp-server-starter") version "1.0.0-rc4"
}

serverStarter {
    gameModeClassName = "ch.leadrian.samp.sadm.SanAndreasDeathmatchGameMode"
    
    // Add a value for an injectable property value,see https://github.com/Netflix/governator/wiki/Configuration-Mapping
    configProperty("my.anti.spam.system.max.messages.per.second", 1)
    
    // Default value is OperatingSystem.current() which depends on your machine
    // Override this value if you want to configure your productive Linux server on a Windows machien for example
    operatingSystem = OperatingSystem.WINDOWS
    
    // Override the download URL if you don't want to use the default download URL
    windowsServerDownloadUrl = "http://files.sa-mp.com/samp037_svr_R2-1-1_win32.zip"

    // Override the download URL if you don't want to use the default download URL
    linuxServerDownloadUrl = "http://files.sa-mp.com/samp037svr_R2-1.tar.gz"

    // Override this if you want to use a Kamp native plugin binary you compiled yourself
    // By default the kamp.so packaged with this Gradle plugin will be deployed, it should contain the newest version
    linuxKampPluginFile = file("path/to/alternative/plugin/binary")
    
    // Override this if you want to use a Kamp native plugin binary you compiled yourself
    // By default the kamp.dll packaged with this Gradle plugin will be deployed, it should contain the newest version
    windowsKampPluginFile = file("path/to/alternative/plugin/binary")

    // Add additional native plugin binaries that will be add to the plugins folder and to server.cfg
    additionalWindowsPlugins(file("path/to/FCNPC.dll"), file("path/to/ColAndreas.dll"))
    
    // Add additional native plugin binaries that will be add to the plugins folder and to server.cfg
    additionalLinuxPlugins(file("path/to/FCNPC.so"), file("path/to/ColAndreas.so"))

    // Add a single JVM option
    jvmOption("-Xmx1G")
    
    // Add multiple JVM options
    jvmOptions("-Xms100M", "-Xmx1G")
    
    // The following properties can be used to configure the corresponding server.cfg values

    lanMode = false
    
    // By default, a random password will be generated
    rconPassword = "supersecretpassword"

    maxPlayers = 100

    port = 7777

    hostName = "San Andreas Deathmatch"

    announce = false

    chatLogging = false

    webUrl = "www.sa-mp.com"

    onFootRate = 40

    inCarRate = 40

    weaponRate = 40

    streamDistance = 300f

    streamRate = 1000

    maxNPCs = 0

    logTimeFormat = "[%H:%M:%S]"

    language = "English"
    
    // Add additional server.cfg values that are not supported by the extension
    additionalServerCfgValue("useartwork", 1)
}
```

For working examples have a look at the [kamp-examples](https://github.com/Double-O-Seven/kamp-examples) repository which contains several example gamemodes.
