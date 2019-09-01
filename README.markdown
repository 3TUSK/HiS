## Historia in Speculō 

(Lat. for "the history in the looking-glass") is a minimalistic crawler and
webserver for counting and displaying the ratio of mod repost threads to
original mod release threads on [www.mcbbs.net](www.mcbbs.net).

## Setup Development Environment

Just import this project as a Gradle project in your IDE (Eclipse, IDEA, etc.).

## Build

```bash
gradle build
```

Alternatively, there is a task that produces a jar with all dependencies shaded:

```bash
gradle shadeJar
```

## Deployment

This little thing expects the runtime environment to have JRE and Redis installed.
Redis server must be running when starting this little thing. The recommended JRE
version is 1.8, but it *may* be possible to use an older JRE.
