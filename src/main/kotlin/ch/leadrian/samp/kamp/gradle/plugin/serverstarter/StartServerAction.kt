package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import org.gradle.api.Action
import org.gradle.api.tasks.Exec
import org.gradle.internal.os.OperatingSystem

open class StartServerAction : Action<Exec> {

    override fun execute(exec: Exec) {
        val serverDirectoryBase = exec.project.buildDir.toPath().resolve(ServerStarterPlugin.SERVER_DIRECTORY_NAME)
        val serverDirectory = if (OperatingSystem.current().isLinux) {
            serverDirectoryBase.resolve("samp03")
        } else {
            serverDirectoryBase
        }
        val commandLine = when {
            OperatingSystem.current().isWindows -> serverDirectory.resolve("samp-server.exe").toString()
            else -> serverDirectory.resolve("samp03svr").toString()
        }
        exec.workingDir(serverDirectory)
        exec.commandLine(commandLine)
    }
}