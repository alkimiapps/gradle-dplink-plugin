package com.alkimiapps.gradle.plugin.dplink

import com.alkimiapps.gradle.plugin.dplink.internal.DplinkConfig
import com.alkimiapps.gradle.plugin.dplink.internal.DplinkExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

/** The Gradle plugin dplink task. */
open class DplinkTask : DefaultTask(), DplinkConfig {
	
	override val buildDir: File = project.buildDir
	@Input override var javaHome = ""
	@Input override var modulesHome = ""
	@Input override var outputDir = ""
	@Input override var executableJar = ""
	@Input override var mainClassName = ""
	@Input override var jvmArgs = ""
	@Input override var appArgs = ""
	@Input override var libs: FileCollection = project.fileTree(buildDir.resolve("libs"))
	@Input override var scriptLocation = "bin/app"
	@Input override var allJavaModules: Boolean = false
	@Input override var fatJar: Boolean = false
	@Input override var verbose: Boolean = false
	
	@TaskAction
	fun run() {
		if (outputDir.isEmpty())
			outputDir = buildDir.resolve("app").toString()
		if(javaHome.isEmpty())
			javaHome = System.getProperty("java.home")
		if(modulesHome.isEmpty())
			modulesHome = System.getProperty("java.home")
		DplinkExecutor(this)
	}
	
}
