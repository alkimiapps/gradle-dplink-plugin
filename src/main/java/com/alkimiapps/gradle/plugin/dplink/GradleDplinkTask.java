package com.alkimiapps.gradle.plugin.dplink;

import com.alkimiapps.gradle.plugin.dplink.internal.DplinkExecutor;
import lombok.Data;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;


public class GradleDplinkTask extends DefaultTask {

    @TaskAction
    public void run() {

        System.out.println("Hello fom gradle dplink task");
        Project project = getProject();
        Path buildFolderPath = project.getBuildDir().toPath();
        System.out.println("Build folder path: : " + buildFolderPath.toAbsolutePath().toString());
        GradleDplinkExtension extension = project.getExtensions().getByType(GradleDplinkExtension.class);
        Path javaHome = new File(extension.getJavaHome()).toPath();
        Path outputDir = new File(extension.getOutputDir()).toPath();

        new DplinkExecutor().dplink(buildFolderPath, javaHome, outputDir, extension.getMainClass(), extension.getExecutableJar());
    }
}
