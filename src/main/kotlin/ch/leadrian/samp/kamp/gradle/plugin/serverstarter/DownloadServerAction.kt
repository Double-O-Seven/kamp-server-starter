package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Action

open class DownloadServerAction : Action<Download> {

    override fun execute(download: Download) {
        with(download) {
            val extension = project.extensions.getByType(ServerStarterPluginExtension::class.java)
            val serverDownloadDirectory = project.buildDir.resolve(ServerStarterPlugin.SERVER_DOWNLOAD_DIRECTORY_NAME)
            src(extension.downloadUrl)
            dest(serverDownloadDirectory.resolve(extension.downloadFileName))
            overwrite(false)
        }
    }
}