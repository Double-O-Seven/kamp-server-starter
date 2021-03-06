package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import com.google.common.io.Resources
import org.apache.commons.lang3.RandomStringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.file.FileLookup
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import javax.inject.Inject

open class ConfigureServerTask
@Inject
constructor(private val fileLookup: FileLookup) : DefaultTask() {

    @get:Nested
    internal val extension: ServerStarterPluginExtension by lazy {
        project.extensions.getByType(ServerStarterPluginExtension::class.java)
    }

    @get:OutputDirectory
    internal val serverDirectory: File by lazy {
        val serverDirectoryBase = project.buildDir.resolve(ServerStarterPlugin.SERVER_DIRECTORY_NAME)
        if (extension.operatingSystem.isLinux) {
            serverDirectoryBase.resolve(ServerStarterPlugin.LINUX_SERVER_DIRECTORY_ROOT)
        } else {
            serverDirectoryBase
        }
    }

    private val serverDownloadDirectory: File
        get() = project.buildDir.resolve(ServerStarterPlugin.SERVER_DOWNLOAD_DIRECTORY_NAME)

    @get:InputFile
    internal val serverDownloadFile: File
        get() = serverDownloadDirectory.resolve(extension.downloadFileName)

    private val gamemodesDirectory: File
        get() = serverDirectory.resolve("gamemodes")

    private val kampAmxFile: File
        get() = gamemodesDirectory.resolve("kamp.amx")

    private val kampDirectory: File
        get() = serverDirectory.resolve("Kamp")

    private val dataDirectory: File
        get() = kampDirectory.resolve("data")

    private val launchDirectory: File
        get() = kampDirectory.resolve("launch")

    private val jarsDirectory: File
        get() = launchDirectory.resolve("jars")

    private val pluginsDirectory: File
        get() = serverDirectory.resolve("plugins")

    private val serverCfgFile: File
        get() = serverDirectory.resolve("server.cfg")

    private val jvmoptsTxtFile: File
        get() = launchDirectory.resolve("jvmopts.txt")

    private val configPropertiesFile: File
        get() = kampDirectory.resolve("config.properties")

    private val kampPluginFileName: String
        get() {
            val os = extension.operatingSystem
            return when {
                os.isWindows -> "kamp.dll"
                os.isLinux -> "libkamp.so"
                else -> throw UnsupportedOperationException("Unsupported operating system: $os")
            }
        }

    private val kampPluginFile: File
        get() = pluginsDirectory.resolve(kampPluginFileName)

    private val windowsKampPluginFile: File?
        get() = extension.windowsKampPluginFile?.takeIf { extension.operatingSystem.isWindows }?.resolveFile()

    private val linuxKampPluginFile: File?
        get() = extension.linuxKampPluginFile?.takeIf { extension.operatingSystem.isLinux }?.resolveFile()

    private val additionalPluginFiles: List<File>
        get() = extension.additionalPluginFiles.map { it.resolveFile() }

    private val sampgdkFileName: String
        get() {
            val os = extension.operatingSystem
            return when {
                os.isWindows -> "sampgdk4.dll"
                os.isLinux -> "libsampgdk.so"
                else -> throw UnsupportedOperationException("Unsupported operating system: $os")
            }
        }

    private val sampgdkFile: File
        get() = serverDirectory.resolve(sampgdkFileName)

    private val runtimeConfiguration: Configuration
        get() = project.configurations.getByName("runtimeClasspath")

    private val jarFiles: Set<File>
        get() = project.tasks.withType(Jar::class.java).mapNotNull { it.archiveFile.orNull?.asFile }.toSet()

    @InputFiles
    fun getInputFiles(): List<File> = setOf(runtimeConfiguration.resolve(), jarFiles).flatten()

    @TaskAction
    fun configureServer() {
        unpackServer()
        createDirectories()
        copyDependencies()
        writeServerCfg()
        createJvmOptsFile()
        createConfigPropertiesFile()
        copyKampPluginFile()
        copyAdditionalPluginFiles()
        copySampgdkFile()
        copyKampAmxFile()
    }

    private fun unpackServer() {
        project.copy { copy ->
            val archive = serverDownloadFile.let {
                when {
                    it.isZipFile() -> project.zipTree(it)
                    it.isTarFile() -> project.tarTree(it)
                    else -> throw UnsupportedOperationException("Unsupported archive: $it")
                }
            }
            copy.from(archive).into(serverDirectory)
        }
    }

    private fun File.isZipFile(): Boolean = nameEndsWith(".zip")

    private fun File.isTarFile(): Boolean = nameEndsWith(".tar.gz")

    private fun File.nameEndsWith(fileEnding: String) = name.endsWith(fileEnding, ignoreCase = true)

    private fun createDirectories() {
        kampDirectory.mkdirs()
        dataDirectory.mkdirs()
        launchDirectory.mkdirs()
        jarsDirectory.mkdirs()
        pluginsDirectory.mkdirs()
    }

    private fun copyDependencies() {
        // Need to delete old jars in case they're outdated regarding version
        jarsDirectory.listFiles().filter { it.isFile }.forEach {
            it.delete()
        }
        runtimeConfiguration.resolve().forEach {
            it.copyTo(jarsDirectory.resolve(it.name))
        }
        jarFiles.forEach { jarFile ->
            jarFile.copyTo(jarsDirectory.resolve(jarFile.name))
        }
    }

    private fun writeServerCfg() {
        FileWriter(serverCfgFile).use { writer ->
            with(writer) {
                write("echo Executing Server Config...\n")
                write("lanmode ${extension.lanMode.toInt()}\n")
                val rconPassword = extension.rconPassword ?: RandomStringUtils.random(8, true, true)
                write("rcon_password $rconPassword\n")
                write("maxplayers ${extension.maxPlayers}\n")
                write("port ${extension.port}\n")
                write("hostname ${extension.hostName}\n")
                write("gamemode0 kamp 1\n")
                val pluginNames = mutableListOf(kampPluginFileName)
                pluginNames += additionalPluginFiles.map { it.name }
                val pluginNamesWithoutDll = when {
                    extension.operatingSystem.isWindows -> pluginNames.map { it.replace(".dll", "", ignoreCase = true) }
                    else -> pluginNames
                }
                write("plugins ${pluginNamesWithoutDll.joinToString(" ")}\n")
                write("announce ${extension.announce.toInt()}\n")
                write("chatlogging ${extension.chatLogging.toInt()}\n")
                write("weburl ${extension.webUrl}\n")
                write("onfoot_rate ${extension.onFootRate}\n")
                write("incar_rate ${extension.inCarRate}\n")
                write("weapon_rate ${extension.weaponRate}\n")
                write("stream_distance ${extension.streamDistance}\n")
                write("stream_rate ${extension.streamRate}\n")
                write("maxnpc ${extension.maxNPCs}\n")
                write("logtimeformat ${extension.logTimeFormat}\n")
                write("language ${extension.language}\n")
                extension.additionalServerCfgValues.forEach { key, value ->
                    write("$key $value\n")
                }
            }
        }
    }

    private fun Boolean.toInt(): Int = if (this) 1 else 0

    private fun createConfigPropertiesFile() {
        FileWriter(configPropertiesFile).use { writer ->
            with(writer) {
                val gameModeClassName = extension.gameModeClassName
                        ?: throw IllegalStateException("gameModeClassName was not set")
                write("kamp.gamemode.class.name=$gameModeClassName\n")
                write("kamp.plugin.name=kamp\n")
                extension.configProperties.forEach { key, value ->
                    write("$key=$value\n")
                }
            }
        }
    }

    private fun createJvmOptsFile() {
        FileWriter(jvmoptsTxtFile).use { writer ->
            with(writer) {
                val classPath = buildClassPath()
                write("-Djava.class.path=$classPath\n")
                extension.jvmOptions.forEach {
                    write(it)
                    write("\n")
                }
            }
        }
    }

    private fun buildClassPath(): String =
            jarsDirectory
                    .listFiles()
                    .filter { it.isFile }
                    .map { File(".").resolve(it.relativeTo(serverDirectory)).toString() }
                    .filter { it.endsWith(".jar", ignoreCase = true) }
                    .joinToString(File.pathSeparator)

    private fun copyKampPluginFile() {
        val windowsKampPluginFile = this.windowsKampPluginFile
        val linuxKampPluginFile = this.linuxKampPluginFile
        when {
            windowsKampPluginFile != null -> project.copy { copy ->
                copy.from(windowsKampPluginFile).rename { kampPluginFileName }.into(pluginsDirectory)
            }
            linuxKampPluginFile != null -> project.copy { copy ->
                copy.from(linuxKampPluginFile).rename { kampPluginFileName }.into(pluginsDirectory)
            }
            else -> copyResource("lib/${extension.operatingSystem.familyName}/$kampPluginFileName", kampPluginFile)
        }
    }

    private fun copyAdditionalPluginFiles() {
        additionalPluginFiles.forEach {
            project.copy { copy ->
                copy.from(it).into(pluginsDirectory)
            }
        }
    }

    private fun Any.resolveFile(): File = fileLookup.fileResolver.resolve(this)

    private fun copySampgdkFile() {
        copyResource("lib/${extension.operatingSystem.familyName}/$sampgdkFileName", sampgdkFile)
    }

    private fun copyKampAmxFile() {
        copyResource("kamp.amx", kampAmxFile)
    }

    private fun copyResource(resourceName: String, destination: File) {
        FileOutputStream(destination).use { outputStream ->
            Resources.copy(javaClass.getResource(resourceName), outputStream)
        }
    }

}