package com.alkimiapps.gradle.plugin.dplink;

import org.gradle.api.Project;
import org.gradle.api.Plugin;

public class GradleDplinkPlugin implements Plugin<Project> {
    private static final String TASK_NAME = "dplinkTask";

    @Override
    public void apply(Project target) {
        target.getExtensions().create("gradleDplinkPlugin", GradleDplinkPlugin.class);
        target.getTasks().create(TASK_NAME, GradleDplinkTask.class);
    }
}
