package com.alkimiapps.gradle.plugin.dplink.internal;

import com.alkimiapps.javatools.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alkimiapps.javatools.Sugar.fatalGuard;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;

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
 * so informs DplinkExecutor to create an executable script for running the app. This script is named "app" and is
 * created in the ${outputDir}/bin directory. If there is no executableJar but there is a mainClassName then DplinkExecutor will
 * expect that there is only a single jar (e.g. a fat jar) in the libs subdirectory and that that jar is an
 * executable jar. If an executableJar is specified and there are multiple jars in the libs subdirectory then these
 * jars are included in the classpath of the executable.
 */
public class DplinkExecutor {

    public void dplink(@Nonnull Path buildFolderPath, @Nonnull Path javaHome, @Nonnull Path outputDir, @Nullable String mainClassName, @Nullable String executableJar) {
        System.out.println("Hello from dplink executor");
        Optional<Stream<Path>> fileListStream = Optional.empty();

        try {

            Path buildLibsDir = Files.createDirectories(buildFolderPath.resolve("libs"));

            fatalGuard(exists(buildLibsDir), "No libs dir at: " + buildLibsDir.getParent().toString());
            fatalGuard(isDirectory(buildLibsDir), "libs is not a directory: " + buildLibsDir.getParent().toString());

            Set<String> dependentJavaModules = new HashSet<>();

            fileListStream = Optional.of(Files.list(buildLibsDir).parallel());
            fileListStream.get()
                    .flatMap(this::dependentJavaModulesOfJar)
                    .forEach(dependentJavaModules::add);


            if (dependentJavaModules.size() > 0) {
                this.jlink(dependentJavaModules, javaHome, outputDir);
                if (mainClassName != null) {
                    this.createApp(buildLibsDir, outputDir, mainClassName, executableJar);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            fileListStream.ifPresent(BaseStream::close);
        }
    }

    private Stream<String> dependentJavaModulesOfJar(@Nonnull Path jarPath) {
        String[] jdepsCommand = {"jdeps", "--list-deps", jarPath.toString()};

        Function<InputStream, Stream<String>> commandOutputProcessing = (InputStream jdepsInputStream) -> {
            BufferedReader jdepsReader = new BufferedReader(new InputStreamReader(jdepsInputStream));
            return jdepsReader.lines()
                    .filter(s -> s.contains("java."))
                    .map(String::trim)
                    .collect(Collectors.toSet())
                    .parallelStream();
        };

        return this.execCommand(jdepsCommand, commandOutputProcessing).orElse(Stream.of());

    }

    private void jlink(@Nonnull Set<String> dependentJavaModules, @Nonnull Path javaHome, @Nonnull Path outputDir) {
        String dependentJavaModulesString = dependentJavaModules.stream()
                .collect(Collectors.joining(","));

        try {
            if (Files.exists(outputDir)) {
                FileUtils.forceDelete(outputDir.toFile());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] jlinkCommand = {
                javaHome.resolve("bin/jlink").toString(),
                "--module-path",
                javaHome.toString() + "/jmods:mlib",
                "--add-modules",
                dependentJavaModulesString,
                "--output",
                outputDir.toString(),
                "--no-header-files",
                "--no-man-pages",
                "--compress=2"

        };

        this.execCommand(jlinkCommand);
    }

    private void createApp(@Nonnull Path buildLibsDir, @Nonnull Path jreDir, @Nonnull String mainClassName, @Nullable String executableJar) {
        Path jreLibPath = jreDir.resolve("lib");

        fatalGuard(exists(jreLibPath), "No lib dir at: " + jreLibPath.getParent().toString());
        fatalGuard(isDirectory(jreLibPath), "lib is not a directory: " + jreLibPath.getParent().toString());

        try {
            String executableJarName = this.executableJarName(buildLibsDir, executableJar);
            String classpath = this.classpath(buildLibsDir, jreLibPath, executableJarName);
            FileUtils.copyDirectory(buildLibsDir.toFile(), jreLibPath.toFile());
            this.makeAppScript(jreDir, executableJarName, mainClassName, classpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeAppScript(@Nonnull Path jrePath, @Nonnull String executableJarName, @Nonnull String mainClass, @Nonnull String classpath) throws IOException {

        String commandString = jrePath.resolve("bin") + "/java -jar " + jrePath.resolve("lib") + "/" + executableJarName + " " + mainClass;
        if (classpath.length() > 0) {
            commandString = commandString + " -cp " + classpath;
        }
        Path appFilePath = Files.createFile(jrePath.resolve("bin/app"));
        try (BufferedWriter writer = Files.newBufferedWriter(appFilePath)) {
            writer.write("#!/usr/bin/env bash\n");
            writer.write(commandString + "\n");
        }

        this.execCommand(new String[]{"chmod", "uog+x", appFilePath.toString()});
    }

    private String classpath(@Nonnull Path buildLibsDir, @Nonnull Path jreLibDir, @Nonnull String executableJarName) throws IOException {
        return Files.list(buildLibsDir)
                .filter(path -> !(executableJarName.equals(path.getFileName().toString())))
                .map(path -> path.getFileName().toString())
                .map(fileName -> jreLibDir.toString() + "/" + fileName)
                .collect(Collectors.joining(":"));
    }

    private String executableJarName(@Nonnull Path jreLibDir, @Nullable String executableJar) {
        Function<Path, String> findExecutableJar = (Path libDir) -> {
            try {
                List<Path> jarFiles = Files.list(libDir).collect(Collectors.toList());
                fatalGuard(jarFiles.size() == 1, "Expected only a single jar in " +
                        jreLibDir.toString() + " but found " + jarFiles.size() + ". Try specifying the executable jar.");
                return jarFiles.get(0).getFileName().toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        String executableJarName = executableJar != null ? executableJar : findExecutableJar.apply(jreLibDir);

        fatalGuard(exists(jreLibDir.resolve(executableJarName)), "Executable jar " +
                jreLibDir.resolve(executableJarName).toString() + " does not exist.");

        return executableJarName;
    }

    private void execCommand(@Nonnull String[] command) {
        this.execCommand(command, null);
    }

    private Optional<Stream<String>> execCommand(@Nonnull String[] command, @Nullable Function<InputStream, Stream<String>> commandOutputProcessing) {
        try {
            Process commandProcess = Runtime.getRuntime().exec(command);
            commandProcess.waitFor(20, TimeUnit.SECONDS);
            try (InputStream commandInputStream = commandProcess.getInputStream()) {
                fatalGuard(commandProcess.exitValue() == 0, () -> {
                    BufferedReader jlinkReader = new BufferedReader(new InputStreamReader(commandInputStream));
                    jlinkReader.lines().forEach(System.err::println);
                    throw new RuntimeException("Command failed with exit code " + commandProcess.exitValue() + ": " +
                            Arrays.stream(command).collect(Collectors.joining(" ")));
                });

                return commandOutputProcessing != null ? Optional.of(commandOutputProcessing.apply(commandInputStream)) : Optional.empty();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
