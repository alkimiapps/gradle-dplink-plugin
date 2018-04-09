# gradle-dplink-plugin

A gradle build task for simplifying creation of custom Java9 jre environments.

[![Build Status](https://travis-ci.org/alkimiapps/gradle-dplink-plugin.svg?branch=master)](https://travis-ci.org/alkimiapps/gradle-dplink-plugin)

## Why?

Java9 brings with it the ability to create custom java runtime execution (JRE) environments tailored to run a specific
app or set of apps. I.e. using the Java Platform Module System (JPMS) aka project Jigsaw.

In order to create a custom jre two command line utilities are required: jdeps and jlink. 

dplink removes the need to invoke jdeps, collate the required java modules and then call jlink specifying those modules
(i.e. dplink does the invocation of jdeps and jlink for you). 

In addition, on unix type platforms, you can tell dplink to embed an executable script inside the created jre that will 
run an executable jar.

## How to use

### JRE only

In it's simplest form add the following to your build.gradle file:

    plugins {
        id "com.alkimiapps.gradle-dplink-plugin" version "0.1"
    }

Then invoke:

    gradle dplink
    
That will build your project and make a jre in your local _build/app_ folder that contains only the java modules on 
which your build is dependent.

### JRE + App executable script

If you're using something that can run a bash shell script and you have an executable jar and you would like the built 
jre to contain it along with an executable script to execute that executable jar, then you need to provide a bit more 
information to dplink in your build.gradle file.

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
    
The executable script is by default: _build/app/bin/app_

### Dplink Task Options

All dplink tasks are optional but, depending on what you want to do, some may be required.

- _mainClassName_ : specifies the fully qualified class name of the main class of an executable jar
- _executableJar_ : the name of the executable jar to use for creating the executable script
- _jvmArgs_ : jvm arguments to use for executing the app (e.g. -Xmx etc)
- _appArgs_ : args that should be passed into the application (note these or more can also be specifed at app execution time i.e on the command line)
- _javaHome_ : absolute path of your java installation (i.e. containing the bin directory with jdeps and jlink) - defaults to _System.getProperty("java.home")_
- _modulesHome_ : absolute path of the java installation that should be the base for the resulting image, used to allow images for alternate operating systems - defaults to _System.getProperty("java.home")_     
- _outputDir_ : path (relative or absolute) specifying where the custom jre should be placed - defaults to _build/app_
- _fatJar_ :  `true` if the `exectuableJar` is a shaded or fat jar so all other jar files in the lib folder will be ignored - defaults to `false`
- _allJavaModules_ : set to `true` only if jdeps should be skipped to speed up plugin execution time, note all available java modules will be copied to the image - defaults to `false`

## ToDo

- More tests and more configurability 
- Better fat jar and thin jar support
- Other stuff I haven't thought of yet

## Links

- Gradle.org plugin publish location: https://plugins.gradle.org/plugin/com.alkimiapps.gradle-dplink-plugin

## Licensing

gradle-dplink-plugin is licensed under MIT license.