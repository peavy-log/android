# Peavy Android

Remote logger and tracer library for Android.

## Install

1. Add the JitPack repository to `repositories` in the project gradle:

    ```groovy
    buildscript {
        repositories {
            maven { url 'https://jitpack.io' }
        }
    }
    ```

2. Add the library as a dependency in the app module gradle:

    ```groovy
    dependencies {
         implementation 'com.github.peavy-log:android:0.9.1'
    }
    ```

## Usage

### Initialise
Preferably initialise the library as early as possible, such as in your `Application`'s `onCreate`.

The only required parameter during initialisation is the remote endpoint to push to.

Other options are optional, and the defaults should suffice.

```kotlin
class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Peavy.init(this, PeavyOptions(
            endpoint = URL("https://my-endpoint.com"),
            logLevel = LogLevel.Debug,
        ))

        // If using Timber too, import and plant a tree:
        Timber.plant(PeavyTree())
    }
}
```

### Logging

To log lines, use the short `Peavy`-object methods. For example:

```kotlin
// Logging a warning line:
Peavy.w("This is a warning")
```

To log lines including an exception, use the second `throwable` argument in each method:

```kotlin
try {
    val result = 1 / 0
} catch (e: Exception) {
    Peavy.e("Something unexpected happened", e)
}
```

For performance sensitive logging, where the message might be expensive to produce, use the base `Peavy.log` method which uses a closure to build the log line.
The execution of the closure will be halted as soon as `level` is set, if it doesn't meet the minimum log level.
This way you can avoid constructing the message if it won't be logged anyway.

```kotlin
Peavy.log {
    level = LogLevel.Trace
    message = someExpensiveMessage()
}
```

`someExpensiveMessage()` will not be executed unless the global log level is `Trace`. 