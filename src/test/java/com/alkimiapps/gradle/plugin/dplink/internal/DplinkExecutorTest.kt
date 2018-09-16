package com.alkimiapps.gradle.plugin.dplink.internal

import com.alkimiapps.javatools.FileUtils
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.collections.FileCollectionAdapter
import org.gradle.api.internal.file.collections.MinimalFileSet
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import org.junit.jupiter.api.Assertions.*

internal class DplinkExecutorTest {
	
	private val buildDir = Paths.get("/tmp/build")
	private val executableJarsPath = Paths.get("src", "test", "resources", "fatjar")
	private val testJarsPath = Paths.get("src", "test", "resources", "testjars")
	private val libsPath = buildDir.resolve("libs")
	private val outputDir = buildDir.resolve("app")
	
	private val dplinkConfig = SimpleConfig()
	
	@BeforeEach
	fun cleanUp() {
		if (Files.exists(buildDir)) {
			FileUtils.forceDelete(buildDir.toFile())
		}
		Files.createDirectories(libsPath)
	}
	
	@Test
	fun testDplinkWithNoExecutableJar() {
		
		assertFalse(Files.exists(outputDir))
		
		FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile())
		
		DplinkExecutor(dplinkConfig).execute()
		
		assertTrue(Files.exists(outputDir))
		assertTrue(Files.exists(outputDir.resolve("bin")))
	}
	
	@Test
	fun testDplinkWithNoExecutableJarAndExistingAppDir() {
		
		assertFalse(Files.exists(outputDir))
		
		Files.createDirectories(outputDir)
		
		FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile())
		
		DplinkExecutor(dplinkConfig).execute()
		
		assertTrue(Files.exists(outputDir))
		assertTrue(Files.exists(outputDir.resolve("bin")))
	}
	
	@Test
	fun testDplinkWithSingleExecutableJar() {
		
		assertFalse(Files.exists(outputDir))
		
		Files.createDirectories(outputDir)
		
		FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile())
		
		dplinkConfig.mainClassName = "app.TakeAPeakDataLoader"
		DplinkExecutor(dplinkConfig).execute()
		
		assertTrue(Files.exists(outputDir))
		assertTrue(Files.exists(outputDir.resolve("bin/app")))
	}
	
	@Test
	fun testDplinkWithMultipleJarsIncludingExecutable() {
		
		assertFalse(Files.exists(outputDir))
		
		Files.createDirectories(outputDir)
		
		FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile())
		FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile())
		
		dplinkConfig.mainClassName = "app.TakeAPeakDataLoader"
		dplinkConfig.executableJarName = "executable-all.jar"
		DplinkExecutor(dplinkConfig).execute()
		
		assertTrue(Files.exists(outputDir.resolve("bin/app")))
	}
	
	@Test
	fun testDplinkWithMultipleJarsIncludingExecutableAndNoNamedJar() {
		
		assertFalse(Files.exists(outputDir))
		
		Files.createDirectories(outputDir)
		
		FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile())
		FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile())
		
		dplinkConfig.mainClassName = "app.TakeAPeakDataLoader"
		
		assertThrows(RuntimeException::class.java
		) { DplinkExecutor(dplinkConfig) }
		
		assertFalse(Files.exists(outputDir.resolve("bin/app")))
	}
	
	//    @Test
	//    void testDplinkWithJvmArgs() throws Exception {
	//
	//        assertFalse(Files.exists(outputDir));
	//
	//        Files.createDirectories(outputDir);
	//
	//        FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());
	//        FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile());
	//
	//        assertThrows(RuntimeException.class,
	//                () -> new DplinkExecutor().dplink(buildDir, null, javaHome, outputDir, "app.TakeAPeakDataLoader", null, null, null, null, false));
	//
	////        assertFalse(Files.exists(outputDir.resolve("bin/app")));
	//    }
}

internal class SimpleConfig : DplinkConfig {
	override val buildDir = File("/tmp/build")
	override val outputDir = buildDir.resolve("app")
	private val libsDir = buildDir.resolve("libs")
	override val scriptsLocation: String
		get() = "bin/app"
	override val javaHome: File
		get() = File(System.getProperty("java.home"))
	override val modulesHome: File
		get() = javaHome
	override val libs: FileCollection
		get() = FileCollectionAdapter(object : MinimalFileSet {
			override fun getFiles(): Set<File> {
				return libsDir.listFiles().toSet()
			}
			override fun getDisplayName(): String {
				return "libs"
			}
		})
	override var executableJarName = ""
	override var mainClassName = ""
	override val jvmArgs: String
		get() = ""
	override val appArgs: String
		get() = ""
	override val allJavaModules: Boolean
		get() = false
	override val fatJar: Boolean
		get() = false
	override val verbose: Boolean
		get() = true
}
