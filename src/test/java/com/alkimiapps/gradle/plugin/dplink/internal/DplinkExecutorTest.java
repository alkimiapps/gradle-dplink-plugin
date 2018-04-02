package com.alkimiapps.gradle.plugin.dplink.internal;

import com.alkimiapps.javatools.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class DplinkExecutorTest {

    private final Path buildDir = Paths.get(new URI("file:///tmp/build"));
    private final Path executableJarsPath = Paths.get("src","test","resources","fatjar");
    private final Path testJarsPath = Paths.get("src","test","resources","testJars");
    private final Path libsPath = Paths.get(buildDir.toString(), "libs");
    private final Path javaHome = new File(System.getProperty("java.home")).toPath();
    private final Path outputDir = new File(buildDir.toFile().getAbsolutePath() + "/app").toPath();

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

        new DplinkExecutor().dplink(buildDir, javaHome, outputDir, null, null);

        assertTrue(Files.exists(outputDir));
        assertTrue(Files.exists(outputDir.resolve("bin")));
    }

    @Test
    void testDplinkWithNoExecutableJarAndExistingAppDir() throws Exception {

        assertFalse(Files.exists(outputDir));

        Files.createDirectories(outputDir);

        FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());

        new DplinkExecutor().dplink(buildDir, javaHome, outputDir, null, null);

        assertTrue(Files.exists(outputDir));
        assertTrue(Files.exists(outputDir.resolve("bin")));
    }

    @Test
    void testDplinkWithSingleExecutableJar() throws Exception {

        assertFalse(Files.exists(outputDir));

        Files.createDirectories(outputDir);

        FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile());

        new DplinkExecutor().dplink(buildDir, javaHome, outputDir, "app.TakeAPeakDataLoader", null);

        assertTrue(Files.exists(outputDir));
        assertTrue(Files.exists(outputDir.resolve("bin/app")));
    }

    @Test
    void testDplinkWithMultipleJarsIncludingExecutable() throws Exception {

        assertFalse(Files.exists(outputDir));

        Files.createDirectories(outputDir);

        FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());
        FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile());

        new DplinkExecutor().dplink(buildDir, javaHome, outputDir, "app.TakeAPeakDataLoader", "executable-all.jar");

        assertTrue(Files.exists(outputDir.resolve("bin/app")));
    }

    @Test
    void testDplinkWithMultipleJarsIncludingExecutableAndNoNamedJar() throws Exception {

        assertFalse(Files.exists(outputDir));

        Files.createDirectories(outputDir);

        FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());
        FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile());

        assertThrows(RuntimeException.class,
                () -> new DplinkExecutor().dplink(buildDir, javaHome, outputDir, "app.TakeAPeakDataLoader", null));

        assertFalse(Files.exists(outputDir.resolve("bin/app")));
    }
}
