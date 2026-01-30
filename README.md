### KMT Test
This is a Kotlin Multiplatform project targeting Desktop (JVM).
It contains a small interpreter plus an interactive editor for the KMT test language.

### Disclaimer

- Large sequences are fully materialized and can hit memory limits; this is a known trade-off. Millions of elements work well; billions can exceed JVM array limits. A lazy strategy would avoid OOM but is slower.
- Performance has been validated using the example program and moderate inputs; extreme stress tests (very large ranges) can still exhaust JVM memory.
- I only have macOS available and did not set up a Windows VM to test the Windows build.

### Structure

* [/shared](./shared/src) contains the interpreter core (lexer, parser, diagnostics, type checker, evaluator).
* [/composeApp](./composeApp/src) contains the Compose Multiplatform UI.

* [/desktopApp](./desktopApp) contains the Desktop (JVM) application entry point.

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :desktopApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :desktopApp:run
  ```

### Run Tests

```shell
./gradlew :shared:jvmTest
```

### Code Style (ktlint)

```shell
./gradlew ktlintCheck
```

```shell
./gradlew ktlintFormat
```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
