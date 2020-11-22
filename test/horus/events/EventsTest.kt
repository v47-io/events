/**
 * BSD 3-Clause License
 *
 * Copyright (c) 2020, Alex Katlein
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
