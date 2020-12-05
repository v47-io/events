# Events 

> Simple asynchronous events for Kotlin
 
![Build](https://github.com/v47-io/events/workflows/Build/badge.svg?branch=master)

## Prerequisites
 - Kotlin 1.4.20
 - Coroutines 1.4.2
 
## Download

The library is available in the Central Repository.

```groovy
repositories {
    maven { mavenCentral() }
}

dependencies {
    implementation 'io.v47:events:1.0.0'
}
```
 
## How-To

This rather simple event library only has two important interfaces: `EventKey` and `EventEmitter`.

`EventKey` is used to uniquely identify an event type and is type-bound to its
payload which can be anything.

```kotlin
import io.v47.events.EventKey

data class StringEvent(val message: String) {
    companion object : EventKey<StringEvent>
}
```

In this case `StringEvent` is the payload and its companion object is the appropriate event key.

This can now be used with the `EventEmitter` interface to emit it.

Events provides a default implementation of `EventEmitter`, `DefaultEventEmitter` which
can be subclassed or delegated to for all your event emitting needs.

The `emit` function of `EventEmitter` is suspending which means that it's going to run in the 
current coroutine context. This gives you full control over how to emit events, whether to block
the current coroutine until all listeners were called, or to launch a new coroutine just to emit
the events.

```kotlin
import io.v47.events.DefaultEventEmitter
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val emitter = DefaultEventEmitter()
    
    // You can either add a permanent listener...
    emitter.on(StringEvent) {
        println(it.message)
    }
    
    // ...or one that's only called once and then discarded
    emitter.once(StringEvent) {
        println(it.message)
    }
    
    emitter.emit(StringEvent, StringEvent("This is printed twice")) // suspending
    emitter.emit(StringEvent, StringEvent("This is printed once")) // suspending
}
```

The expected output is:

```text
This is printed twice
This is printed twice
This is printed once
```

## License

Events is released under the terms of the BSD 3-clause license
