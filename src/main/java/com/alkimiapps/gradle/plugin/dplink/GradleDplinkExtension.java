package com.alkimiapps.gradle.plugin.dplink;

import lombok.Data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@Data
class GradleDplinkExtension {
    private @Nonnull String javaHome = System.getProperty("java.home");
    private @Nonnull String outputDir = "build/app";
    private @Nullable String executableJar;
    private @Nullable String mainClass;
}
