# Peavy Android

Remote logger and tracer library for Android.

## Install

1. Add the JitPack repository to `repositories` in the settings gradle:

    ```groovy
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            maven { url 'https://jitpack.io' }
        }
    }
    ```

2. Add the library as a dependency in the app module gradle:

    ```groovy
    dependencies {
         implementation 'com.github.peavy-log:android:0.9.16'
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

### Metadata

To automatically add metadata to log lines, use `Peavy.setMeta()`:

```kotlin
Peavy.setMeta("userId" to myUser.id)
```

### Network Traces

To automatically get tracing on network calls, which - with cooperation from the backend -
will correlate frontend requests with backend logs, add the included `PeavyTracingInterceptor` OkHttp interceptor:

```kotlin
val client = OkHttpClient.Builder()
   .addInterceptor(PeavyTracingInterceptor())
   .build()
```

Depending on backend, a different implementation of tracing data might be required.

You can implement your own by subclassing the `PeavyTracing` class and implementing its methods.

The default implementation is generic W3C.

Also available is a Google Cloud specific one, which in addition to adding W3C headers,
also adds legacy GC-specific data.

```kotlin
val client = OkHttpClient.Builder()
   .addInterceptor(PeavyTracingInterceptor(tracer = PeavyTracing.GoogleCloud))
   .build()
```
