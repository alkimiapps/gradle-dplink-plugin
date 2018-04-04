package com.alkimiapps.gradle.plugin.dplink.internal;

import lombok.Data;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Data
public class DplinkConfig {
    private @Nonnull Path buildFolderPath = Paths.get("build");
    private @Nonnull Path buildLibsDir = buildFolderPath.resolve("libs");
    private @Nonnull Path javaHome = Paths.get(System.getProperty("java.home"));
    private @Nonnull Path outputDir = buildFolderPath.resolve("app");
    private @Nonnull Optional<String> executableJar = Optional.empty();
    private @Nonnull Optional<String>  mainClassName = Optional.empty();
    private @Nonnull Optional<String>  jvmArgs = Optional.empty();
    private @Nonnull Optional<String>  appArgs = Optional.empty();
    private @Nonnull String appName = "app";
    private boolean verbose;
}

