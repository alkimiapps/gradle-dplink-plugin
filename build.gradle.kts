import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	java
	maven
	`java-gradle-plugin`
	id("org.jetbrains.kotlin.jvm") version "1.2.70"
	id("com.gradle.plugin-publish") version "0.10.0"
}

repositories {
	jcenter()
}

group = "com.alkimiapps"
version = "0.4"
val pluginId = "$group.gradle-dplink-plugin"

java.sourceCompatibility = JavaVersion.VERSION_1_9
java.targetCompatibility = JavaVersion.VERSION_1_9

dependencies {
	compile(kotlin("stdlib"))
	
	compileOnly(gradleApi())
	
	testCompile("org.mockito:mockito-core:2.+")
	testCompile("org.junit.jupiter:junit-jupiter-api:5.+")
	testRuntime("org.junit.jupiter:junit-jupiter-engine:5.+")
}

gradlePlugin {
	plugins.create("gradleDplinkPlugin") {
		id = pluginId
		implementationClass = "com.alkimiapps.gradle.plugin.dplink.GradleDplinkPlugin"
	}
}

pluginBundle {
	website = "https://github.com/alkimiapps/gradle-dplink-plugin"
	vcsUrl = "https://github.com/alkimiapps/gradle-dplink-plugin"
	description = "A gradle build task for simplifying creation of custom Java9 jre environments."
	tags = listOf("java9", "modules", "jigsaw", "jpms", "jdeps", "jlink")
	
	plugins["gradleDplinkPlugin"].run {
		id = pluginId
		displayName = "Gradle Dplink plugin"
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<JavaCompile> {
	sourceCompatibility = "1.9"
	targetCompatibility = "1.9"
}
