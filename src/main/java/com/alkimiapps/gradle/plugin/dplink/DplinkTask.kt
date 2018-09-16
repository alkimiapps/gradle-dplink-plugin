package com.alkimiapps.gradle.plugin.dplink

import com.alkimiapps.gradle.plugin.dplink.internal.DplinkConfig
import com.alkimiapps.gradle.plugin.dplink.internal.DplinkExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.*
import java.io.File

/** The Gradle dplink plugin task. */
open class DplinkTask : DefaultTask(), DplinkConfig {
	
	final override val buildDir: File = project.buildDir
	@Input override var javaHome = File(System.getProperty("java.home"))
	@Input override var modulesHome = javaHome
	@Input override var outputDir = buildDir.resolve("app")
	@Input override var executableJarName = ""
	@Input override var mainClassName = ((project as ExtensionAware).extensions.getByName("application") as? JavaApplication)?.mainClassName ?: ""
	@Input override var jvmArgs = ""
	@Input override var appArgs = ""
	@InputFiles override var libs: FileCollection = project.fileTree(buildDir.resolve("libs"))
	@Input override var scriptsLocation = "bin/app"
	@Input override var allJavaModules: Boolean = false
	@Input override var fatJar: Boolean = false
	@Input override var verbose: Boolean = false
	
	@TaskAction
	fun run() {
		DplinkExecutor(this).execute()
	}
	
}
