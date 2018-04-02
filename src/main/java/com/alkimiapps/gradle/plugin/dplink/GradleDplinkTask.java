package com.alkimiapps.gradle.plugin.dplink;

import com.alkimiapps.gradle.plugin.dplink.internal.DplinkExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;

@Data
@EqualsAndHashCode(callSuper = true)
public class GradleDplinkTask extends DefaultTask {

    private @Nonnull @Input String javaHome = System.getProperty("java.home");
    private @Nonnull @Input String outputDir = "build/app";
    private @Nullable @Input String executableJar = "";
    private @Nullable @Input String mainClassName = "";


    @TaskAction
    public void run() {

        Project project = getProject();
        Path buildFolderPath = project.getBuildDir().toPath();
        Path javaHome = new File(this.getJavaHome()).toPath();
        Path outputDir = new File(this.getOutputDir()).toPath();

        new DplinkExecutor().dplink(buildFolderPath, javaHome, outputDir, this.getMainClassName(), this.getExecutableJar());
    }
}
