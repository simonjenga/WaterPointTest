Water Point Code Challenge
============================

Consider a dataset providing information on the functionality of infrastructure resources, for each water point it includes the name of the village it is in and its functional state.

Implement a data processing module which takes a dataset URL as input and returns:
- The number of water points that are functional,
- The number of water points per community,
- The rank for each community by the percentage of broken water points.

There should be a top level object or function ```calculate("http://...")```, which returns a data structure with the above information, something like:

```
{
  number_functional: ...,
  number_water_points: {
    communityA: ...,
  },
  community_ranking: ...
}
```

But that’s just a suggestion and we can think of alternative, maybe better, data structures to use.

We will use a water point dataset that contains many columns, the relevant ones are:

```communities_villages, water_functioning```

The data is at the below URL:
https://raw.githubusercontent.com/onaio/ona-tech/master/data/water_points.json

Building The Project
====================

The project compiles with ```JDK >= 1.5``` and ```Maven >= 3.1.1``` as the build tool and to also manage the project dependencies.

To run Maven build, execute the following from a console/command prompt with the project root directory as the top level directory:

```mvn clean package```

This will create a distributable and executable JAR file for the standalone application.

Then run the JAR file created by executing the following:

```java -jar target/WaterPointTest.jar```

Results of the API query will be logged to the console window.

To run Unit tests, execute the following:

```mvn clean test```

### Instructions

Eclipse users run `mvn eclipse:eclipse` and then import the project or just import the code as a Maven project into IntelliJ, NetBeans, or Eclipse.

To generate project documentation of Java source files (Javadoc), run `mvn javadoc:javadoc`
