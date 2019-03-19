package com.alkimiapps.gradle.plugin.dplink.internal

import java.io.File
import java.util.concurrent.TimeUnit
import java.util.stream.Stream


fun failIf(condition: Boolean, message: String) {
	if (condition)
		throw RuntimeException(message)
}

/**
 * Executes the commands that create a jre customised to contain only those Java modules needed for some specific
 * application.
 * <p>
 * The application is defined in terms of one or more jars within libs subdirectory of a build folder (i.e. standard
 * gradle location for storing jars required at runtime). DplinkExecutor iterates over the jars the libs subdirectory
 * and finds the java dependencies for each jar. Then using that list of dependencies, jlink is executed to create the
 * custom jre in the outputDir location (e.g. build/app).
 * <p>
 * Optionally the name of an executable jar and the name of a main class for an executable jar can be specified. Doing
 * so informs DplinkExecutor to create an executable script for running the app. This script is by default named "app" and is
 * created in the ${outputDir}/bin directory. If there is no executableJar but there is a mainClassName then DplinkExecutor will
 * expect that there is only a single jar in the libs subdirectory and that that jar is an executable jar. If an
 * executableJar is specified and there are multiple jars in the libs subdirectory then these jars are included in the
 * classpath of the executable.
 */
class DplinkExecutor(val config: DplinkConfig) {
	val javaHome = config.javaHome
	val modulesHome = config.modulesHome
	val outputDir = config.outputDir
	val libs = config.libs.files.flatMap { if (it.isFile) listOf(it) else it.listFiles().filter { it.isFile } }
	val executableJarName = config.executableJarName.takeUnless { it.isEmpty() }
	val tmpDir = config.buildDir.resolve("tmp").resolve("dplink").also {
		it.deleteRecursively()
		it.mkdirs()
	}
	val pathToRoot = "../".repeat(config.scriptsLocation.count { it == '/' }).takeUnless { it.isEmpty() } ?: "./"
	
	fun execute() {
		val dependentJavaModules = ArrayList<String>()
		if (config.allJavaModules) {
			allJavaModules().forEach { dependentJavaModules.add(it) }
		} else {
			if (config.fatJar && executableJarName != null) {
				listOf(File(executableJarName))
			} else {
				libs
			}.forEach { dependentJavaModules.addAll(dependentJavaModulesOfJar(it)) }
		}
		
		if (dependentJavaModules.isNotEmpty()) {
			jlink(dependentJavaModules, outputDir)
			if (config.mainClassName.isNotEmpty())
				createApp()
		}
	}
	
	private fun allJavaModules(): List<String> {
		val javaCommand = arrayOf(javaHome.resolve("bin/java"), "--list-modules", "--module-path", modulesHome.resolve("jmods"))
		return execCommand("allJavaModules", *javaCommand).trim().map { it.replaceFirst("@.*$".toRegex(), "") }
		
	}
	
	fun execCommand(name: String?, vararg command: Any): List<String> {
		if (config.verbose)
			println("Executing: " + command.joinToString(" "))
		val builder = ProcessBuilder(command.map { it.toString() }).redirectError(ProcessBuilder.Redirect.INHERIT)
		var stdout: File? = null
		if (name != null) {
			stdout = tmpDir.resolve("stdout_$name.txt")
			builder.redirectOutput(stdout)
		}
		val process = builder.start()
		process.waitFor(2, TimeUnit.MINUTES)
		if (process.exitValue() != 0)
			throw RuntimeException("Command failed with exit code ${process.exitValue()}: ${command.joinToString(" ")}\n" +
					"Logs can be found at $tmpDir")
		return stdout?.readLines() ?: listOf()
	}
	
	private fun jlink(modules: Collection<String>, outputDir: File) {
		if (outputDir.exists())
			outputDir.deleteRecursively()
		this.execCommand(null, bin("jlink"), "--module-path", "$modulesHome/jmods:mlib", "--add-modules", modules.joinToString(","), "--output", outputDir, "--no-header-files", "--no-man-pages", "--compress=2")
	}
	
	private fun dependentJavaModulesOfJar(jar: File): List<String> =
			execCommand("jdeps-${jar.nameWithoutExtension}", bin("jdeps"), "--list-deps", jar)
					.filter { s -> s.matches("^\\s*(java|jdk|javafx|oracle)\\..*$".toRegex()) }.trim()
					.map { s -> s.replaceFirst("/.*$".toRegex(), "") }
	
	
	private fun createApp() {
		val appLibs = outputDir.resolve("lib").resolve("libs")
		failIf(config.mainClassName.isEmpty(), "Missing main class name - needed for executable jar")
		
		val jar = executableJar()
		if (config.verbose)
			println("Copying $libs to $appLibs")
		libs.forEach { it.copyTo(appLibs.resolve(it.name), true) }
		
		val jvmArgs = config.jvmArgs
		val appArgs = config.appArgs
		
		makeAppScript(config.mainClassName, jvmArgs, jar.name, appArgs, outputDir)
		
	}
	
	private fun executableJar(): File {
		val jar = libs.find { it.name == executableJarName } ?: run {
			failIf(libs.size != 1, "Expected exactly one jar in libs. Please specify executableJarName.")
			libs[0]
		}
		failIf(!jar.exists(), "Executable jar $jar does not exist.")
		return jar
	}
	
	private fun makeAppScript(mainClass: String, jvmArgs: String, executableJarName: String, appArgs: String, outputDir: File) {
		val commandString = "${pathToRoot}bin/java $jvmArgs -jar ${pathToRoot}lib/libs/$executableJarName $mainClass $appArgs -cp ${pathToRoot}lib/libs"
		
		val unixExec = outputDir.resolve(config.scriptsLocation)
		val winExec = unixExec.resolveSibling(unixExec.nameWithoutExtension + ".bat")
		
		unixExec.writeText("#!/usr/bin/env bash\n$commandString \"$@\"")
		winExec.writeText("$commandString %*")
		
		unixExec.setExecutable(true)
		winExec.setExecutable(true)
	}
	
	fun <T> ArrayList<T>.addAll(stream: Stream<T>) = stream.forEach { add(it) }
	
	fun bin(name: String) = javaHome.resolve("bin/$name")
	
	fun List<String>.trim() = map { it.trim { it <= ' ' } }
	
}