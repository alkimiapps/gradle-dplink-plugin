package com.alkimiapps.gradle.plugin.dplink;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.alkimiapps.gradle.plugin.dplink.internal.DplinkConfig;
import com.alkimiapps.gradle.plugin.dplink.internal.DplinkExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import static com.alkimiapps.javatools.Strings.hasChars;
import static com.alkimiapps.javatools.Sugar.ifThen;
import static java.util.Optional.of;

/**
 * The Gradle plugin dplink task.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GradleDplinkTask extends DefaultTask {

    // Gradle insists that all @Input properties must have a value. So, we use "" to indicate no value.
    private @Input
    String javaHome = "";
    private @Input
    String modulesHome = "";
    private @Input
    String outputDir = "";
    private @Input
    String executableJar = "";
    private @Input
    String mainClassName = "";
    private @Input
    String jvmArgs = "";
    private @Input
    String appArgs = "";
    private @Input
    String appName = "";
    private @Input
    boolean allJavaModules;
    private @Input
    boolean fatJar;
    private @Input
    boolean verbose;

    @TaskAction
    public void run() {

        Project project = getProject();
        Path buildFolderPath = project.getBuildDir().toPath().toAbsolutePath();

        DplinkConfig dplinkConfig = new DplinkConfig();
        dplinkConfig.setBuildFolderPath(buildFolderPath);
        dplinkConfig.setBuildLibsDir(buildFolderPath.resolve("libs"));
        dplinkConfig.setOutputDir(buildFolderPath.resolve("app"));
        ifThen(hasChars(this.getAppArgs()), () -> dplinkConfig.setAppArgs(of(this.getAppArgs())));
        ifThen(hasChars(this.getJvmArgs()), () -> dplinkConfig.setJvmArgs(of(this.getJvmArgs())));
        ifThen(hasChars(this.getMainClassName()), () -> dplinkConfig.setMainClassName(of(this.getMainClassName())));
        ifThen(hasChars(this.getJavaHome()), () -> dplinkConfig.setJavaHome(Paths.get(this.getJavaHome())));
        ifThen(hasChars(this.getModulesHome()), () -> dplinkConfig.setModulesHome(Paths.get(this.getModulesHome())));
        ifThen(hasChars(this.getOutputDir()), () -> dplinkConfig.setOutputDir(Paths.get(this.getOutputDir())));
        ifThen(hasChars(this.getExecutableJar()), () -> dplinkConfig.setExecutableJar(of(this.getExecutableJar())));
        ifThen(hasChars(this.getAppName()), () -> dplinkConfig.setAppName(this.getAppName()));
        Optional<ResolvableDependencies> runtimeDependencies = this.runtimeDependencies();
        if (runtimeDependencies.isPresent()) {
            System.out.println("#### " + runtimeDependencies.get().getPath());
            FileCollection fileCollection = runtimeDependencies.get().getFiles();
            fileCollection.getFiles().forEach(file -> { System.out.println("**** " + file.getAbsolutePath());});
        }
        dplinkConfig.setRuntimeDependencies(this.runtimeDependencies());
        dplinkConfig.setAllJavaModules(this.allJavaModules);
        dplinkConfig.setFatJar(this.fatJar);
        dplinkConfig.setVerbose(this.verbose);

        new DplinkExecutor().dplink(dplinkConfig);
    }

    private Optional<ResolvableDependencies> runtimeDependencies() {
        ConfigurationContainer configurations = getProject().getConfigurations();
        return configurations.getNames().stream()
                .filter(name -> name.equals("runtime"))
                .limit(1)
                // Calling getByName if the named config doesn't exist causes an exception which is why we filter through getNames first
                .map(runtime -> configurations.getByName("runtime"))
                .map(Configuration::getIncoming)
                .reduce((a, b) -> a);
    }

}
