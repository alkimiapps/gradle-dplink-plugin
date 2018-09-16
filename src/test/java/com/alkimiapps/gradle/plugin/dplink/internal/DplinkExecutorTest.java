package com.alkimiapps.gradle.plugin.dplink.internal;

import com.alkimiapps.javatools.FileUtils;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.FileCollectionAdapter;
import org.gradle.api.internal.file.collections.MinimalFileSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DplinkExecutorTest {
	
	private final Path buildDir = Paths.get("/tmp/build");
	private final Path executableJarsPath = Paths.get("src", "test", "resources", "fatjar");
	private final Path testJarsPath = Paths.get("src", "test", "resources", "testjars");
	private final Path libsPath = buildDir.resolve("libs");
	private final Path outputDir = buildDir.resolve("app");
	
	private SimpleConfig dplinkConfig = new SimpleConfig();
	
	DplinkExecutorTest() throws URISyntaxException {}
	
	@BeforeEach
	void cleanUp() throws IOException {
		if (Files.exists(buildDir)) {
			FileUtils.forceDelete(buildDir.toFile());
		}
		Files.createDirectories(libsPath);
	}
	
	@Test
	void testDplinkWithNoExecutableJar() throws Exception {
		
		assertFalse(Files.exists(outputDir));
		
		FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());
		
		new DplinkExecutor(dplinkConfig).execute();
		
		assertTrue(Files.exists(outputDir));
		assertTrue(Files.exists(outputDir.resolve("bin")));
	}
	
	@Test
	void testDplinkWithNoExecutableJarAndExistingAppDir() throws Exception {
		
		assertFalse(Files.exists(outputDir));
		
		Files.createDirectories(outputDir);
		
		FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());
		
		new DplinkExecutor(dplinkConfig).execute();
		
		assertTrue(Files.exists(outputDir));
		assertTrue(Files.exists(outputDir.resolve("bin")));
	}
	
	@Test
	void testDplinkWithSingleExecutableJar() throws Exception {
		
		assertFalse(Files.exists(outputDir));
		
		Files.createDirectories(outputDir);
		
		FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile());
		
		dplinkConfig.mainClassName = "app.TakeAPeakDataLoader";
		new DplinkExecutor(dplinkConfig).execute();
		
		assertTrue(Files.exists(outputDir));
		assertTrue(Files.exists(outputDir.resolve("bin/app")));
	}
	
	@Test
	void testDplinkWithMultipleJarsIncludingExecutable() throws Exception {
		
		assertFalse(Files.exists(outputDir));
		
		Files.createDirectories(outputDir);
		
		FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());
		FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile());
		
		dplinkConfig.mainClassName = "app.TakeAPeakDataLoader";
		dplinkConfig.executableJarName = "executable-all.jar";
		new DplinkExecutor(dplinkConfig).execute();
		
		assertTrue(Files.exists(outputDir.resolve("bin/app")));
	}
	
	@Test
	void testDplinkWithMultipleJarsIncludingExecutableAndNoNamedJar() throws Exception {
		
		assertFalse(Files.exists(outputDir));
		
		Files.createDirectories(outputDir);
		
		FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());
		FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile());
		
		dplinkConfig.mainClassName = "app.TakeAPeakDataLoader";
		
		assertThrows(RuntimeException.class,
				() -> new DplinkExecutor(dplinkConfig));
		
		assertFalse(Files.exists(outputDir.resolve("bin/app")));
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

class SimpleConfig implements DplinkConfig {
	private final Path buildDir = Paths.get("/tmp/build");
	private final Path outputDir = buildDir.resolve("app");
	private final Path libsPath = buildDir.resolve("libs");
	@NotNull
	@Override
	public File getBuildDir() {
		return buildDir.toFile();
	}
	@NotNull
	@Override
	public File getOutputDir() {
		return outputDir.toFile();
	}
	@NotNull
	@Override
	public String getScriptsLocation() {
		return "bin/app";
	}
	@NotNull
	@Override
	public File getJavaHome() {
		return new File(System.getProperty("java.home"));
	}
	@NotNull
	@Override
	public File getModulesHome() {
		return getJavaHome();
	}
	@NotNull
	@Override
	public FileCollection getLibs() {
		return new FileCollectionAdapter(new MinimalFileSet() {
			@Override
			public Set<File> getFiles() {
				try {
					return Files.list(libsPath).map(Path::toFile).collect(Collectors.toSet());
				} catch(IOException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			public String getDisplayName() {
				return "libs";
			}
		});
	}
	String executableJarName = "";
	@NotNull
	@Override
	public String getExecutableJarName() {
		return executableJarName;
	}
	String mainClassName = "";
	@NotNull
	@Override
	public String getMainClassName() {
		return mainClassName;
	}
	@NotNull
	@Override
	public String getJvmArgs() {
		return "";
	}
	@NotNull
	@Override
	public String getAppArgs() {
		return "";
	}
	@Override
	public boolean getAllJavaModules() {
		return false;
	}
	@Override
	public boolean getFatJar() {
		return false;
	}
	@Override
	public boolean getVerbose() {
		return true;
	}
}
