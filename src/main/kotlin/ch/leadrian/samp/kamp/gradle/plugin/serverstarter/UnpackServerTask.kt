package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class UnpackServerTask : DefaultTask() {

    private val extension: ServerStarterPluginExtension
        get() = project.extensions.getByType(ServerStarterPluginExtension::class.java)

    private val serverDirectory = project.buildDir.resolve(ServerStarterPlugin.SERVER_DIRECTORY_NAME)

    private val serverDownloadDirectory = project.buildDir.resolve(ServerStarterPlugin.SERVER_DOWNLOAD_DIRECTORY_NAME)

    private val serverDownloadFile: File
        get() = serverDownloadDirectory.resolve(extension.downloadFileName)

    @InputFile
    fun getInputFile(): File = serverDownloadFile

    @OutputDirectory
    fun getOutputDirectory(): File = serverDirectory

    @TaskAction
    fun unpackServer() {
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
}
