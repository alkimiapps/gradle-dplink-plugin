# gradle-dplink-plugin

A gradle build task for simplifying creation of custom Java9 jre environments.

## Why?

Java9 brings with it the ability to create custom java runtime execution (JRE) environments tailored to run a specific
app or set of apps. I.e. using the Java Platform Module System (JPMS) aka project Jigsaw.

In order to create a custom jre two command line utilities are required: jdeps and jlink. 

dplink removes the need to invoke jdeps, collate the required java modules and then call jlink specifying those modules
(i.e. dplink does the invocation of jdeps and jlink for you). 

In addition you can tell dplink to embed an executable script inside the created jre that will run an executable
jar.

## Getting started

In it's simplest form add the following to your build.gradle file:

    plugins {
        id "com.alkimiapps.gradle-dplink-plugin" version "0.1"
    }

Then invoke:

    gradle dplink
    
That will build your project and make a jre in your local _build/app_ folder that contains only the java modules on 
which your build is dependent.

If you have an executable jar and you would like the built jre to contain it along with an executable script
to execute that executable jar, then you need to provide a bit more information to dplink in your build.gradle file.

At minimum you need to specify the main class name e.g (in your build.gradle file)

    dplink {
        mainClassName="my.package.name.MyMainClassName"
    }
    
I.e the java package and class name of the main class of the executable jar.

If your _build/libs_ folder contains a single jar then dplink assumes that it the executable jar and it will use that.
On the other hand if your _build/libs_  contains multiple jars you'll need to tell dplink which one is the executable
e.g:

    dplink {
        mainClassName="my.package.name.MyMainClassName"
        executableJar="MyExecutableJar.jar"
    }
    
# Links

- Gradle.org plugin publish location: https://plugins.gradle.org/plugin/com.alkimiapps.gradle-dplink-plugin

## Licensing

gradle-dplink-plugin is licensed under MIT license.