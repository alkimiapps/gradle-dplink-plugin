package com.alkimiapps.gradle.plugin.dplink;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.Task;

public class GradleDplinkPlugin implements Plugin<Project> {
    private static final String TASK_NAME = "dplink";

    @Override
    public void apply(Project project) {
        Task dplinkTask = project.getTasks().create(TASK_NAME, GradleDplinkTask.class);
        dplinkTask.dependsOn(project.getTasksByName("build", false));
    }
}
