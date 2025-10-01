EscherJava - HTTP request signing lib ![Build Status](https://github.com/emartech/escher-java/actions/workflows/java.yml/badge.svg)
=====================================

Java implementation of the [Escher](https://github.com/emartech/escher) HTTP request signing library

Prerequisite
------------

If you clone the repository for the first time, run the following commands to initialize the test cases as well:
```shell
# after git clone stay in the root of the project
git submodule init
git submodule update
```

Update the repository with the following command to have the test cases as well:
```shell
git pull origin master --recurse-submodules
```

To update the remote test cases to the latest state, run the following:
```shell
 git submodule update --remote test-cases
```

Development
-------------

In order to compile the project, **Maven** and **JDK 11** (or above) are needed.

To check if maven is configured properly, run the `mvn --version` command and look at the line beginning with *Java version*.

Run the tests
-------------

To run all the tests, use the `mvn test` command or `make test` if you have Docker installed.

Release to Central Repository
-------------

To release the bundle.jar to the central maven repository check the [RELEASE.md file](./RELEASE.md)

About Escher
------------

More details are available at our [Escher documentation site](http://escherauth.io/).
