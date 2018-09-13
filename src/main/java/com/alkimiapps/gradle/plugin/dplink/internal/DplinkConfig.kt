package com.alkimiapps.gradle.plugin.dplink.internal

import org.gradle.api.file.FileCollection
import java.io.File

interface DplinkConfig {
	val buildDir: File
	val outputDir: String
	val scriptLocation: String
	
	val javaHome: String
	val modulesHome: String
	
	val libs: FileCollection
	
	val executableJar: String
	val mainClassName: String
	val jvmArgs: String
	val appArgs: String
	
	val allJavaModules: Boolean
	val fatJar: Boolean
	val verbose: Boolean
}