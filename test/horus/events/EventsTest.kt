/**
 * Copyright 2020 The Horus Events Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package horus.events

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventsTest {
    private val emitCheckList = listOf(
            StringEvent("This is a string!"),
            StringEvent("This is a string!"),
            StringEvent("This is another string!")
    )

    private val removeCheckList = listOf(
            StringEvent("Event no. 1"),
            StringEvent("Event no. 1"),
            StringEvent("Event no. 2"),
            StringEvent("Event no. 2"),
            StringEvent("Event no. 3")
    )

    @Test
    fun emitTest() {
        val events = mutableListOf<StringEvent>()

        val emitter = DefaultEventEmitter()
        emitter.on(StringEvent) {
            events += it
        }
        emitter.once(StringEvent) {
            events += it
        }

        runBlocking {
            emitter.emit(StringEvent, StringEvent("This is a string!"))
            emitter.emit(StringEvent, StringEvent("This is another string!"))
        }

        emitter.clear()

        assertIterableEquals(emitCheckList, events)
    }

    @Test
    fun emitBlockingTest() {
        val events = mutableListOf<StringEvent>()

        val emitter = DefaultEventEmitter()
        emitter.on(StringEvent) {
            events += it
        }
        emitter.once(StringEvent) {
            events += it
        }

        emitter.emitBlocking(StringEvent, StringEvent("This is a string!"))
        emitter.emitBlocking(StringEvent, StringEvent("This is another string!"))

        emitter.clear(StringEvent)

        assertIterableEquals(emitCheckList, events)
    }

    @Test
    fun removeListenerTest() {
        val events = mutableListOf<StringEvent>()

        val emitter = DefaultEventEmitter()
        emitter.on(StringEvent) {
            events += it
        }

        val transientListener: (suspend (StringEvent) -> Unit) = {
            events += it
        }

        emitter.on(StringEvent, transientListener)

        runBlocking {
            emitter.emit(StringEvent, StringEvent("Event no. 1"))
            emitter.emit(StringEvent, StringEvent("Event no. 2"))

            emitter.remove(transientListener)

            emitter.emit(StringEvent, StringEvent("Event no. 3"))
        }

        assertIterableEquals(removeCheckList, events)
    }
}

data class StringEvent(val string: String) {
    companion object : EventKey<StringEvent>
}
