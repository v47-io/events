package horus.events

import kotlinx.coroutines.experimental.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventsTest {
    @Test
    fun emitTest() {
        val emitter = DefaultEventEmitter()
        emitter.on(StringEvent) {
            println("on $StringEvent")
            println(it.string)
        }
        emitter.once(StringEvent) {
            println("once $StringEvent")
            println(it.string)
        }

        runBlocking {
            emitter.emit(StringEvent, StringEvent("This is a string!"))
            emitter.emit(StringEvent, StringEvent("This is another string!"))
        }

        emitter.clear()
    }

    @Test
    fun emitBlockingTest() {
        val emitter = DefaultEventEmitter()
        emitter.on(StringEvent) {
            println("on $StringEvent")
            println(it.string)
        }
        emitter.once(StringEvent) {
            println("once $StringEvent")
            println(it.string)
        }

        emitter.emitBlocking(StringEvent, StringEvent("This is a string!"))
        emitter.emitBlocking(StringEvent, StringEvent("This is another string!"))

        emitter.clear(StringEvent)
    }

    @Test
    fun removeListenerTest() {
        val emitter = DefaultEventEmitter()
        emitter.on(StringEvent) {
            println("Persistent listener")
        }

        val transientListener: (suspend (StringEvent) -> Unit) = {
            println("Transient listener")
        }

        emitter.on(StringEvent, transientListener)

        runBlocking {
            emitter.emit(StringEvent, StringEvent("Event no. 1"))
            emitter.emit(StringEvent, StringEvent("Event no. 2"))

            emitter.remove(transientListener)

            emitter.emit(StringEvent, StringEvent("Event no. 3"))
        }
    }
}

data class StringEvent(val string: String) {
    companion object : EventKey<StringEvent>
}
