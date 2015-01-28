# Gasp

Gasp is a lightweight spatial server powered by PostGIS.

Most application servers are designed to abstract away the underlying database 
that powers them. This both limits the functionality the database can provide 
to the end user and leads to unnecessary "middleware bloat". Gasp takes the 
opposite approach and embraces the power of the database. The idea is to let the 
database do things it is good at and let web services do things they are good at.

## Building Gasp

Gasp used [Gradle](https://gradle.org/) as a build system. To build simply 
run Gradle in this directory:

    gradle build

In addition to Gradle, Gasp also requires [Node.js](http://nodejs.org/) in 
order to build the front-end web application. Once node and `npm` are installed
install both [Bower](http://bower.io/) and [Grunt](http://gruntjs.com/).