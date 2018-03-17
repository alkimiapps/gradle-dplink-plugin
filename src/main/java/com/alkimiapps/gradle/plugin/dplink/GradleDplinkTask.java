package com.alkimiapps.gradle.plugin.dplink;

import com.alkimiapps.gradle.plugin.dplink.internal.DplinkExecutor;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.ArtifactHandler;
import org.gradle.api.tasks.TaskAction;

public class GradleDplinkTask extends DefaultTask {
    private DplinkExecutor dplinkExecutor = new DplinkExecutor();

    @TaskAction
    public void dplinkTask() {
        Project project = getProject();
        ArtifactHandler artifactHandler = project.getArtifacts();
        dplinkExecutor.execute();
    }
}
