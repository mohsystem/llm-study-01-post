# Gradle Wrapper Jar Note

This repository intentionally excludes `gradle-wrapper.jar` because the PR transport used in this environment does not support binary files.

To regenerate the wrapper jar locally (or in CI), run:

```bash
gradle wrapper --gradle-version 8.10.2
```

This will recreate `gradle/wrapper/gradle-wrapper.jar` compatible with `gradle/wrapper/gradle-wrapper.properties`.
