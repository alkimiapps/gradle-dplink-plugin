package com.alkimiapps.gradle.plugin.dplink;

import org.gradle.internal.impldep.org.junit.Rule;
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.File;

import static org.gradle.internal.impldep.org.testng.internal.Utils.writeFile;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class GradleDplinkPluginTest {
    private static final String TASK_NAME = "dplinkTask";

    @Rule
    private final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void test() throws Exception {
        setUpTestProject();

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath()
                .withArguments(TASK_NAME, "--stacktrace")
                .build();

        assertThat(
                result.task(":" + TASK_NAME).getOutcome(),
                equalTo(SUCCESS));
    }

    private void setUpTestProject() throws Exception {
        testProjectDir.create();
        File buildFile = testProjectDir.newFile("build.gradle");
        writeFile(buildFile.getParent(), buildFile.getName(),"plugins { id 'gradle-dplink-plugin' }");
    }
}
