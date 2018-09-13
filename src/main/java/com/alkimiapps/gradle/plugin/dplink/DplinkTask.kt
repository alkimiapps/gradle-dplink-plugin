package com.alkimiapps.gradle.plugin.dplink

import com.alkimiapps.gradle.plugin.dplink.internal.DplinkConfig
import com.alkimiapps.gradle.plugin.dplink.internal.DplinkExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Paths

/** The Gradle plugin dplink task. */
open class DplinkTask : DefaultTask() {
	
	// Gradle insists that all @Input properties must have a value. So, we use "" to indicate no value.
	@Input var javaHome = ""
	@Input var modulesHome = ""
	@Input var outputDir = ""
	@Input var executableJar = ""
	@Input var mainClassName = ""
	@Input var jvmArgs = ""
	@Input var appArgs = ""
	@Input var appName = ""
	@Input var allJavaModules: Boolean = false
	@Input var fatJar: Boolean = false
	@Input var verbose: Boolean = false
	
	@TaskAction
	fun run() {
		val project = project
		val buildFolderPath = project.buildDir.toPath().toAbsolutePath()
		
		val dplinkConfig = DplinkConfig()
		dplinkConfig.buildFolderPath = buildFolderPath
		dplinkConfig.buildLibsDir = buildFolderPath.resolve("libs")
		dplinkConfig.outputDir = buildFolderPath.resolve("app")
		if (appArgs.isNotEmpty()) {
			dplinkConfig.appArgs = this.appArgs
		}
		if (jvmArgs.isNotEmpty()) {
			dplinkConfig.jvmArgs = this.jvmArgs
		}
		if (mainClassName.isNotEmpty()) {
			dplinkConfig.mainClassName = this.mainClassName
		}
		if (javaHome.isNotEmpty()) {
			dplinkConfig.javaHome = Paths.get(this.javaHome)
		}
		if (modulesHome.isNotEmpty()) {
			dplinkConfig.modulesHome = Paths.get(this.modulesHome)
		}
		if (outputDir.isNotEmpty()) {
			dplinkConfig.outputDir = Paths.get(this.outputDir)
		}
		if (executableJar.isNotEmpty()) {
			dplinkConfig.executableJar = this.executableJar
		}
		if (appName.isNotEmpty()) {
			dplinkConfig.appName = this.appName
		}
		dplinkConfig.allJavaModules = this.allJavaModules
		dplinkConfig.fatJar = this.fatJar
		dplinkConfig.verbose = this.verbose
		
		DplinkExecutor().dplink(dplinkConfig)
	}
}
