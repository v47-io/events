/**
 * BSD 3-Clause License
 *
 * Copyright (c) 2023, Alex Katlein
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
package io.v47.events

import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The event key is used to uniquely identify an event that is emitted by the event emitter.
 *
 * It also specifies the type of the event payload to enable implementation of type-safe handlers.
 *
 * @param T The type of the event payload
 *
 * @since Events 1.0
 */
interface EventKey<out T : Any>

/**
 * The event emitter asynchronously calls the listeners for the specified event key
 * and provides the specified payload to them.
 *
 * It's possible to add listeners that are called everytime, and some that are only
 * called once and then removed.
 *
 * @since Events 1.0
 */
interface EventEmitter {
    /**
     * Checks whether there are listeners (both persistent and once) for the
     * specified event key.
     *
     * @param key The key uniquely identifying an event
     *
     * @since Events 2.1
     */
    fun hasListeners(key: EventKey<*>): Boolean

    /**
     * Emits the event identified by the event key with the specified payload
     *
     * @param key The key uniquely identifying the event
     * @param payload Data that is passed to the listeners
     * @param T The type of the payload
     *
     * @since Events 1.0
     */
    suspend fun <T : Any> emit(key: EventKey<T>, payload: T)

    /**
     * Emits the event identified by the event with the payload created by the
     * specified builder.
     *
     * The emitter may choose to not even call the builder if there are no listeners
     * for the specified event key.
     *
     * This is useful when the payload is rather expensive to build, and it's not
     * certain there will be a listener for that particular event key
     *
     * @param key The key uniquely identifying the event
     * @param payloadBuilder The function building the actual payload
     * @param T The type of the payload
     *
     * @since Events 2.1
     */
    suspend fun <T : Any> emit(key: EventKey<T>, payloadBuilder: () -> T) {
        emit(key, payloadBuilder())
    }

    /**
     * Registers a listener for the specified event key that is called everytime
     *
     * @param key The event key
     * @param block The listener for the event's payload
     * @param T The type of the event's payload
     *
     * @since Events 1.0
     */
    fun <T : Any> on(key: EventKey<T>, block: (suspend (T) -> Unit))

    /**
     * Registers a listener for the specified event key that is called
     * only once and then discarded.
     *
     * @param key The event key
     * @param block The listener for the event's payload
     * @param T The type of the event's payload
     *
     * @since Events 1.0
     */
    fun <T : Any> once(key: EventKey<T>, block: (suspend (T) -> Unit))

    /**
     * Discards all listenerl for the specified event key.
     *
     * If not key is specified all listeners are discarded.
     *
     * @param key The event key
     *
     * @since Events 1.0
     */
    fun clear(key: EventKey<*>? = null)

    /**
     * Removes the specified listener
     *
     * @param listener
     *
     * @since Events 1.0
     */
    fun <T : Any> remove(listener: (suspend (T) -> Unit))
}

/**
 * Emits the event identified by the specified key with the payload in a blocking
 * fashion. This uses the specified coroutine context.
 *
 * @param key The event key
 * @param payload The payload passed to the listeners
 * @param coroutineContext The coroutine context for the blocking call
 * @param T The type of the payload
 *
 * @since Events 1.0
 */
fun <T : Any> EventEmitter.emitBlocking(
    key: EventKey<T>,
    payload: T,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) {
    runBlocking(coroutineContext) {
        emit(key, payload)
    }
}
