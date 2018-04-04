package com.alkimiapps.gradle.plugin.dplink;

import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.gradle.internal.impldep.org.hamcrest.core.IsEqual.equalTo;
import static org.gradle.internal.impldep.org.junit.Assert.assertThat;
import static org.gradle.internal.impldep.org.testng.internal.Utils.writeFile;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

class GradleDplinkPluginTest {

    private static final String TASK_NAME = "dplink";

    private final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    void test() throws Exception {
        setUpTestProject();

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .forwardOutput()
                .withPluginClasspath()
                .withArguments(TASK_NAME, "--stacktrace")
                .build();

        List<BuildTask> tasks = result.getTasks();

        assertThat(
                result.task(":" + TASK_NAME).getOutcome(),
                equalTo(SUCCESS));
    }

    private void setUpTestProject() throws Exception {
        testProjectDir.create();
        File buildFile = testProjectDir.newFile("build.gradle");
        writeFile(buildFile.getParent(), buildFile.getName(), "plugins { id 'com.alkimiapps.gradle-dplink-plugin' }\n");
        File buildFolder = testProjectDir.newFolder("build");
    }
}
