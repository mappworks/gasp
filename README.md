# Gasp

Gasp is a lightweight spatial server powered by PostGIS.

Most application servers are designed to abstract away the underlying database 
that powers them. This both limits the functionality the database can provide 
to the end user and leads to unnecessary "middleware bloat". Gasp takes the 
opposite approach and embraces the power of the database. The idea is to let the 
database do things it is good at and let web services do things they are good at.

## Building Gasp

Gasp used [Gradle](https://gradle.org/) as a build system. This directory 
contains the [wrapper](https://gradle.org/docs/current/userguide/gradle_wrapper.html) 
scripts needed to build without installing Gradle separately. 

Aside from Gradle the following additional pre-requisites are required:

* [JDK 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Node.js](http://nodejs.org/) and [npm](https://www.npmjs.com/)
* [Bower](http://bower.io/)
* [Grunt](http://gruntjs.com/)

To build run the wrapper script in this directory:

    % gradlew build

The first run of this command will take some time as Gradle bootstraps itself. 
Subsequent calls will run more quickly. 

## Running Gasp

Building Gasp creates a war file named ``gasp.war`` in the ``app/build/lib``
directory. Deploy this war into any servlet container to run the application. 
*Note* that a Servlet 3.0+ compatible container is required.

Alternatively Gasp can be run directly from the development environment by 
running the ``gasp.app.Run`` class in the app module. 