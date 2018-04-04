package com.alkimiapps.gradle.plugin.dplink.internal;

import com.alkimiapps.javatools.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Optional.of;
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
    private final Path testJarsPath = Paths.get("src","test","resources","testjars");
    private final Path libsPath = buildDir.resolve("libs");
    private final Path outputDir = buildDir.resolve("app");

    private DplinkConfig dplinkConfig;

    DplinkExecutorTest() throws URISyntaxException {}

    @BeforeEach
    void cleanUp() throws IOException {
        if (Files.exists(buildDir)) {
            FileUtils.forceDelete(buildDir.toFile());
        }
        Files.createDirectories(libsPath);

        dplinkConfig = new DplinkConfig();
        dplinkConfig.setBuildFolderPath(this.buildDir);
        dplinkConfig.setBuildLibsDir(this.libsPath);
        dplinkConfig.setOutputDir(this.outputDir);
        dplinkConfig.setVerbose(true);
    }

    @Test
    void testDplinkWithNoExecutableJar() throws Exception {

        assertFalse(Files.exists(outputDir));

        FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());


        new DplinkExecutor().dplink(dplinkConfig);

        assertTrue(Files.exists(outputDir));
        assertTrue(Files.exists(outputDir.resolve("bin")));
    }

    @Test
    void testDplinkWithNoExecutableJarAndExistingAppDir() throws Exception {

        assertFalse(Files.exists(outputDir));

        Files.createDirectories(outputDir);

        FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());

        new DplinkExecutor().dplink(dplinkConfig);

        assertTrue(Files.exists(outputDir));
        assertTrue(Files.exists(outputDir.resolve("bin")));
    }

    @Test
    void testDplinkWithSingleExecutableJar() throws Exception {

        assertFalse(Files.exists(outputDir));

        Files.createDirectories(outputDir);

        FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile());

        dplinkConfig.setMainClassName(of("app.TakeAPeakDataLoader"));
        new DplinkExecutor().dplink(dplinkConfig);

        assertTrue(Files.exists(outputDir));
        assertTrue(Files.exists(outputDir.resolve("bin/app")));
    }

    @Test
    void testDplinkWithMultipleJarsIncludingExecutable() throws Exception {

        assertFalse(Files.exists(outputDir));

        Files.createDirectories(outputDir);

        FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());
        FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile());

        dplinkConfig.setMainClassName(of("app.TakeAPeakDataLoader"));
        dplinkConfig.setExecutableJar(of("executable-all.jar"));
        new DplinkExecutor().dplink(dplinkConfig);

        assertTrue(Files.exists(outputDir.resolve("bin/app")));
    }

    @Test
    void testDplinkWithMultipleJarsIncludingExecutableAndNoNamedJar() throws Exception {

        assertFalse(Files.exists(outputDir));

        Files.createDirectories(outputDir);

        FileUtils.copyDirectory(testJarsPath.toFile(), libsPath.toFile());
        FileUtils.copyDirectory(executableJarsPath.toFile(), libsPath.toFile());

        dplinkConfig.setMainClassName(of("app.TakeAPeakDataLoader"));

        assertThrows(RuntimeException.class,
                () -> new DplinkExecutor().dplink(dplinkConfig));

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
