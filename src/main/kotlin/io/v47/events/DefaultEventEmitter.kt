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
package io.v47.events

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * This is the default implementation of an event emitter which can be extended
 * or delegated to.
 *
 * It provides the option to fail fast which means that exceptions thrown
 * by event listeners aren't logged, but rethrown immediately and further listeners
 * are not called.
 *
 * @param failFast Indicates whether to rethrow exceptions caused by event handlers
 *
 * @since Events 1.0
 */
open class DefaultEventEmitter(private val failFast: Boolean = false) : EventEmitter {
    private val log = LoggerFactory.getLogger(javaClass)!!

    private val listeners =
        ConcurrentHashMap<EventKey<*>, ConcurrentLinkedQueue<suspend (Any) -> Unit>>()

    private val listenersOnce =
        ConcurrentHashMap<EventKey<*>, ConcurrentLinkedQueue<suspend (Any) -> Unit>>()

    override suspend fun <T : Any> emit(key: EventKey<T>, payload: T) {
        listeners[key]?.toList()?.callAll(payload)

        listenersOnce[key]?.let { listenersQueue ->
            val listeners = listenersQueue.toList()
            listenersQueue.clear()

            listeners.callAll(payload)
        }
    }

    private suspend fun Iterable<suspend (Any) -> Unit>.callAll(payload: Any) =
        coroutineScope {
            forEach {
                launch {
                    runCatching {
                        it(payload)
                    }.onFailure { x ->
                        if (failFast)
                            throw x
                        else
                            log.warn("Exception caught in listener", x)
                    }
                }
            }
        }

    override fun <T : Any> on(key: EventKey<T>, block: suspend (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        listeners
            .computeIfAbsent(key) { ConcurrentLinkedQueue() }
            .add(block as (suspend (Any) -> Unit))
    }

    override fun <T : Any> once(key: EventKey<T>, block: suspend (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        listenersOnce
            .computeIfAbsent(key) { ConcurrentLinkedQueue() }
            .add(block as (suspend (Any) -> Unit))
    }

    override fun clear(key: EventKey<*>?) {
        if (key != null) {
            listeners.remove(key)
            listenersOnce.remove(key)
        } else {
            listeners.clear()
            listenersOnce.clear()
        }
    }

    override fun <T : Any> remove(listener: suspend (T) -> Unit) {
        listeners.entries.removeIf { (_, listeners) ->
            listeners.remove(listener)
            listeners.isEmpty()
        }

        listenersOnce.entries.removeIf { (_, listeners) ->
            listeners.remove(listener)
            listeners.isEmpty()
        }
    }
}
