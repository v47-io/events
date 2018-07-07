package horus.events

import org.slf4j.LoggerFactory

open class DefaultEventEmitter : EventEmitter {
    private val log = LoggerFactory.getLogger(javaClass)!!

    private val listeners = mutableMapOf<EventKey<*>, MutableList<suspend (Any) -> Unit>>()
    private val listenersOnce = mutableMapOf<EventKey<*>, MutableList<suspend (Any) -> Unit>>()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun <T : Any> emit(key: EventKey<T>, payload: T) {
        listeners[key]?.forEach {
            try {
                it(payload)
            } catch (x: Exception) {
                log.warn("Caught exception in listener", x)
            }
        }

        listenersOnce[key]?.onEach {
            try {
                it(payload)
            } catch (x: Exception) {
                log.warn("Caught exception in listener", x)
            }
        }?.removeAll { true }
    }

    override fun <T : Any> on(key: EventKey<T>, block: suspend (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        listeners.computeIfAbsent(key) { mutableListOf() }.add(block as (suspend (Any) -> Unit))
    }

    override fun <T : Any> once(key: EventKey<T>, block: suspend (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        listenersOnce.computeIfAbsent(key) { mutableListOf() }.add(block as (suspend (Any) -> Unit))
    }

    override fun clear(key: EventKey<*>?) {
        if (key != null) {
            listeners[key]?.removeAll { true }
            listenersOnce[key]?.removeAll { true }
        } else {
            listeners.forEach { it.value.removeAll { true } }
            listenersOnce.forEach { it.value.removeAll { true } }
        }
    }

    override fun <T : Any> remove(listener: suspend (T) -> Unit) {
        listeners.forEach { it.value.remove(listener) }
        listenersOnce.forEach { it.value.remove(listener) }
    }
}
