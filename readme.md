# Java 8 Examples

## Overview
This project contains Java code examples for the major new language features that were added to Java 8.

The examples are implemented as a set of unit tests, themselves implemented using JUnit (Hamcrest and AssertJ).

## Code
The source code for the examples can be found in the src/test/java folder.

## Building and Running the Examples
Support is provided for building and running the project using either Gradle (see build.gradle) or Maven 
(see pom.xml).

### Gradle

To compile and run all the example tests, enter the  following command in the project's root folder:
```gradle clean test```

To generate the Javadoc use, the following command: 
```gradle clean test javadocTests```

### Maven

To compile and run all the example tests, enter the following command in the project's root folder:
```mvn clean test```

To generate the Javadoc, use the following command: 
```mvn javadoc:test-javadoc```

---

(https://bitbucket.org/neilbrown)