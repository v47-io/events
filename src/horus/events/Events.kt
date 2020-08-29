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
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The event key is used to uniquely identify an event that is emitted by the event emitter.
 *
 * It also specifies the type of the event payload so it's possible to implement type-safe handlers
 *
 * @param T The type of the event payload
 *
 * @since Horus Events 1.0.0
 */
@SinceKotlin("1.0")
interface EventKey<out T : Any>

/**
 * The event emitter asynchronously calls the listeners for the specified event key
 * and provides the specified payload to them.
 *
 * It's possible to add listeners that are called everytime, and some that are only
 * called once and then removed.
 *
 * @since Horus Events 1.0.0
 */
@SinceKotlin("1.3")
interface EventEmitter {
    /**
     * Emits the event identified by the event key with the specified payload
     *
     * @param key The key uniquely identifying the event
     * @param payload Data that is passed to the listeners
     * @param T The type of the payload
     *
     * @since Horus Events 1.0.0
     */
    suspend fun <T : Any> emit(key: EventKey<T>, payload: T)

    /**
     * Registers a listener for the specified event key that is called everytime
     *
     * @param key The event key
     * @param block The listener for the event's payload
     * @param T The type of the event's payload
     *
     * @since Horus Events 1.0.0
     */
    fun <T : Any> on(key: EventKey<T>, block: (suspend (T) -> Unit))

    /**
     * Registers a listener for the specified event key that is called
     * only once and then discarded
     *
     * @param key The event key
     * @param block The listener for the event's payload
     * @param T The type of the event's payload
     *
     * @since Horus Events 1.0.0
     */
    fun <T : Any> once(key: EventKey<T>, block: (suspend (T) -> Unit))

    /**
     * Discards all listener for the specified event key.
     *
     * Discards all listener if no key is specified
     *
     * @param key The event key
     *
     * @since Horus Events 1.0.0
     */
    fun clear(key: EventKey<*>? = null)

    /**
     * Removes the specified listener
     *
     * @param listener
     *
     * @since Horus Events 1.0.0
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
 * @since Horus Events 1.0.0
 */
@SinceKotlin("1.3")
fun <T : Any> EventEmitter.emitBlocking(key: EventKey<T>,
                                        payload: T,
                                        coroutineContext: CoroutineContext = EmptyCoroutineContext
) {
    runBlocking(coroutineContext) {
        emit(key, payload)
    }
}
