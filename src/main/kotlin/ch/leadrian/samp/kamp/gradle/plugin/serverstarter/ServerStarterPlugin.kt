package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.jvm.tasks.Jar

open class ServerStarterPlugin : Plugin<Project> {

    companion object {

        const val SERVER_DIRECTORY_NAME = "samp-server"
        const val SERVER_DOWNLOAD_DIRECTORY_NAME = "download"
    }

    override fun apply(project: Project) {
        project.extensions.create("serverStarter", ServerStarterPluginExtension::class.java)
        val downloadTasks = project.tasks.create("downloadServer", Download::class.java, DownloadServerAction())
        val unpackTask = project.tasks.create("unpackServer", UnpackServerTask::class.java).dependsOn(downloadTasks)
        val configureTask = project.tasks.create("configureServer", ConfigureServerTask::class.java).dependsOn(unpackTask)
        project.tasks.create("startServer", Exec::class.java, StartServerAction()).dependsOn(configureTask)
        project.tasks.withType(Jar::class.java) { configureTask.dependsOn(it) }
    }

}