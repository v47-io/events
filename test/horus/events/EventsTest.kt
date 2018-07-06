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
}

data class StringEvent(val string: String) {
    companion object : EventKey<StringEvent>
}
