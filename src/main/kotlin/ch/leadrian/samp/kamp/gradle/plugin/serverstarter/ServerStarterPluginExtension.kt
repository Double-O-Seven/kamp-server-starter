package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import org.apache.commons.lang3.RandomStringUtils
import org.gradle.internal.os.OperatingSystem
import java.net.URI

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class ServerStarterPluginExtension {

    var gameModeClassName: String? = null

    internal val configProperties: MutableMap<String, Any> = mutableMapOf()

    fun configProperty(key: String, value: Any) {
        configProperties[key] = value
    }

    var operatingSystem: OperatingSystem = OperatingSystem.current()

    var windowsServerDownloadUrl: String = "http://files.sa-mp.com/samp037_svr_R2-1-1_win32.zip"

    var linuxServerDownloadUrl: String = "http://files.sa-mp.com/samp037svr_R2-1.tar.gz"

    var linuxKampPluginFile: Any? = null

    var windowsKampPluginFile: Any? = null

    private val additionalWindowsPluginFiles: MutableList<Any> = mutableListOf()

    private val additionalLinuxPluginFiles: MutableList<Any> = mutableListOf()

    fun additionalWindowsPlugins(vararg pluginFiles: Any) {
        additionalWindowsPluginFiles.addAll(pluginFiles)
    }

    fun additionalLinuxPlugins(vararg pluginFiles: Any) {
        additionalLinuxPluginFiles.addAll(pluginFiles)
    }

    internal val additionalPluginFiles: List<Any> = when {
        operatingSystem.isWindows -> additionalWindowsPluginFiles
        operatingSystem.isLinux -> additionalLinuxPluginFiles
        else -> emptyList()
    }

    internal val downloadUrl: String
        get() = when {
            operatingSystem.isWindows -> windowsServerDownloadUrl
            operatingSystem.isLinux -> linuxServerDownloadUrl
            else -> throw UnsupportedOperationException("Unsupported operating system: $operatingSystem")
        }

    internal val downloadFileName: String
        get() = URI(downloadUrl).toURL().file.removePrefix("/")

    internal val jvmOptions: MutableList<String> = mutableListOf()

    fun jvmOption(value: String) {
        jvmOptions += value
    }

    fun jvmOptions(vararg values: String) {
        jvmOptions += values
    }

    var lanMode: Boolean = false

    var rconPassword: String = RandomStringUtils.random(8, true, true)

    var maxPlayers: Int = 100

    var port: Int = 7777

    var hostName: String = "SA-MP 0.3.7 Server"

    var announce: Boolean = false

    var chatLogging: Boolean = false

    var webUrl: String = "www.sa-mp.com"

    var onFootRate: Int = 40

    var inCarRate: Int = 40

    var weaponRate: Int = 40

    var streamDistance: Float = 300f

    var streamRate: Int = 1000

    var maxNPCs: Int = 0

    var logTimeFormat: String = "[%H:%M:%S]"

    var language: String = "English"

    internal val additionalServerCfgValues: MutableMap<String, Any> = mutableMapOf()

    fun additionalServerCfgValue(key: String, value: Any) {
        additionalServerCfgValues[key] = value
    }

}