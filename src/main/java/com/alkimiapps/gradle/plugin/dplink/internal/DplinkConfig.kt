package com.alkimiapps.gradle.plugin.dplink.internal

import java.nio.file.Paths

class DplinkConfig {
	var javaHome = Paths.get(System.getProperty("java.home"))
	var modulesHome = Paths.get(System.getProperty("java.home"))
	var buildFolderPath = Paths.get("build")
	var buildLibsDir = buildFolderPath.resolve("libs")
	var outputDir = buildFolderPath.resolve("app")
	var executableJar: String? = null
	var mainClassName: String? = null
	var jvmArgs: String? = null
	var appArgs: String? = null
	var appName = "app"
	var allJavaModules: Boolean = false
	var fatJar: Boolean = false
	var verbose: Boolean = false
}

