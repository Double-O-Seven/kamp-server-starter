package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import org.apache.commons.lang3.RandomStringUtils
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.internal.os.OperatingSystem
import java.net.URI

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class ServerStarterPluginExtension {

    @get:[Optional Input]
    var gameModeClassName: String? = null

    @get:Input
    internal val configProperties: MutableMap<String, Any> = mutableMapOf()

    fun configProperty(key: String, value: Any) {
        configProperties[key] = value
    }

    @get:Input
    var operatingSystem: OperatingSystem = OperatingSystem.current()

    var windowsServerDownloadUrl: String = "http://files.sa-mp.com/samp037_svr_R2-1-1_win32.zip"

    var linuxServerDownloadUrl: String = "http://files.sa-mp.com/samp037svr_R2-1.tar.gz"

    @get:[Optional InputFile]
    var linuxKampPluginFile: Any? = null

    @get:[Optional InputFile]
    var windowsKampPluginFile: Any? = null

    private val additionalWindowsPluginFiles: MutableList<Any> = mutableListOf()

    private val additionalLinuxPluginFiles: MutableList<Any> = mutableListOf()

    fun additionalWindowsPlugins(vararg pluginFiles: Any) {
        additionalWindowsPluginFiles.addAll(pluginFiles)
    }

    fun additionalLinuxPlugins(vararg pluginFiles: Any) {
        additionalLinuxPluginFiles.addAll(pluginFiles)
    }

    @get:InputFiles
    internal val additionalPluginFiles: List<Any> = when {
        operatingSystem.isWindows -> additionalWindowsPluginFiles
        operatingSystem.isLinux -> additionalLinuxPluginFiles
        else -> emptyList()
    }

    @get:Input
    internal val downloadUrl: String
        get() = when {
            operatingSystem.isWindows -> windowsServerDownloadUrl
            operatingSystem.isLinux -> linuxServerDownloadUrl
            else -> throw UnsupportedOperationException("Unsupported operating system: $operatingSystem")
        }

    internal val downloadFileName: String
        get() = URI(downloadUrl).toURL().file.removePrefix("/")

    @get:Input
    internal val jvmOptions: MutableList<String> = mutableListOf()

    fun jvmOption(value: String) {
        jvmOptions += value
    }

    fun jvmOptions(vararg values: String) {
        jvmOptions += values
    }

    @get:Input
    var lanMode: Boolean = false

    @get:[Optional Input]
    var rconPassword: String? = null

    @get:Input
    var maxPlayers: Int = 100

    @get:Input
    var port: Int = 7777

    @get:Input
    var hostName: String = "SA-MP 0.3.7 Server"

    @get:Input
    var announce: Boolean = false

    @get:Input
    var chatLogging: Boolean = false

    @get:Input
    var webUrl: String = "www.sa-mp.com"

    @get:Input
    var onFootRate: Int = 40

    @get:Input
    var inCarRate: Int = 40

    @get:Input
    var weaponRate: Int = 40

    @get:Input
    var streamDistance: Float = 300f

    @get:Input
    var streamRate: Int = 1000

    @get:Input
    var maxNPCs: Int = 0

    @get:Input
    var logTimeFormat: String = "[%H:%M:%S]"

    @get:Input
    var language: String = "English"

    @get:Input
    internal val additionalServerCfgValues: MutableMap<String, Any> = mutableMapOf()

    fun additionalServerCfgValue(key: String, value: Any) {
        additionalServerCfgValues[key] = value
    }

}