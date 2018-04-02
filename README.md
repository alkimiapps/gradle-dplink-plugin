# gradle-dplink-plugin #

A gradle build task for simplifying creation of custom Java9 jre environments.

## What problem does dplink try to solve? ##

Java9 brings with it the ability to create custom java runtime execution (JRE) environments tailored to run a specific
app or set of apps. I.e. using the Java Platform Module System (JPMS) aka project Jigsaw.

In order to create a custom jre two command line utilities are required: jdeps and jlink. 

dplink removes the need to invoke jdeps, collate the required java modules and then call jlink specifying those modules
(i.e. dplink does the invocation of jdeps and jlink for you). 

In addition you can tell dplink to embed an executable script inside the created jre that will run an executable
jar.

## Getting started ##


 

* Quick summary
* Version
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact