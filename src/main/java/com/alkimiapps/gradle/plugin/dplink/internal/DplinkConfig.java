package com.alkimiapps.gradle.plugin.dplink.internal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javax.annotation.Nonnull;

import lombok.Data;
import org.gradle.api.artifacts.ResolvableDependencies;

@Data
public class DplinkConfig {
    private @Nonnull Path javaHome = Paths.get(System.getProperty("java.home"));
    private @Nonnull Path modulesHome = Paths.get(System.getProperty("java.home"));
    private @Nonnull Path buildFolderPath = Paths.get("build");
    private @Nonnull Path buildLibsDir = buildFolderPath.resolve("libs");
    private @Nonnull Path outputDir = buildFolderPath.resolve("app");
    private @Nonnull Optional<String> executableJar = Optional.empty();
    private @Nonnull Optional<String>  mainClassName = Optional.empty();
    private @Nonnull Optional<String>  jvmArgs = Optional.empty();
    private @Nonnull Optional<String>  appArgs = Optional.empty();
    private @Nonnull String appName = "app";
    private @Nonnull Optional<ResolvableDependencies> runtimeDependencies;
    private boolean allJavaModules;
    private boolean fatJar;
    private boolean verbose;

    public DplinkConfig() {}
}

