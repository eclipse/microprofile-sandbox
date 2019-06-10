## Boost

Boost is a Maven plugin that enhances the builds for your MicroProfile applications.

There are two separate Boost projects. 

- Boost Maven Plugin, BOMs, and Boosters (`boost-maven`)
- Boost Common Resources (`boost-common`)

### Developing Boost

If you are interested in contributing to Boost, read the [wiki](https://github.com/OpenLiberty/boost/wiki) for more information.

If you are interested in the Boost runtime adapter mechanism, it is described in greater detail in the [Boost Runtimes](https://github.com/OpenLiberty/boost/wiki/Boost-Runtimes) page of the wiki.

### Building Boost

You will need to build the `boost-common` project before building the `boost-maven` project. We provide some scripts below to simplify this process. 

#### Boost Maven Plugin

To build the Boost Maven Plugin:

##### Windows:

```
./boost-maven.bat
```

##### Mac/Linux:

```
./boost-maven.sh
```
