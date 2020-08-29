# Horus Events 

> Simple asynchronous events for Kotlin 

[![Download](https://api.bintray.com/packages/vemilyus/horus/horus-events/images/download.svg)][bintray-url]

[bintray-url]: https://bintray.com/vemilyus/horus/horus-events/_latestVersion

## Prerequisites
 - Kotlin 1.4.0
 - Coroutines 1.3.9
 
## Download

The library is available on bintray.

```groovy
repositories {
    maven { url 'https://dl.bintray.com/vemilyus/horus' }
}

dependencies {
    compile 'com.vemilyus.horus:horus-events:1.5.0'
}
```
 
## How-To

This rather simple event emitter only has two important interfaces: `EventKey` and `EventEmitter`.

`EventKey` is used to uniquely identify an event type and is type-bound to its \
payload which can be anything.

```kotlin
data class StringEvent(val message: String) {
    companion object : EventKey<StringEvent>
}
```

In this case `StringEvent` is the payload and its companion object is the appropriate event key.

This can now be used with the `EventEmitter` interface to emit it.

Horus Events provides a default implementation of `EventEmitter`, `DefaultEventEmitter` which
can be subclasses or delegated to for all your event emitting needs.

```kotlin
suspend fun main() {
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

```
This is printed twice
This is printed twice
This is printed once
```

## License

Horus Events is released under the terms of the Apache Software License 2.0
