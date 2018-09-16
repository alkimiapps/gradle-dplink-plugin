package com.alkimiapps.gradle.plugin.dplink.internal

import org.gradle.api.file.FileCollection
import java.io.File

interface DplinkConfig {
	val buildDir: File
	val outputDir: File
	val scriptsLocation: String
	
	val libs: FileCollection
	val javaHome: File
	val modulesHome: File
	val executableJarName: String
	
	val mainClassName: String
	val jvmArgs: String
	val appArgs: String
	
	val allJavaModules: Boolean
	val fatJar: Boolean
	val verbose: Boolean
}