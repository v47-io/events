package horus.events

open class DefaultEventEmitter : EventEmitter {
    private val listeners = mutableMapOf<EventKey<*>, MutableList<suspend (Any) -> Unit>>()
    private val listenersOnce = mutableMapOf<EventKey<*>, MutableList<suspend (Any) -> Unit>>()

    override suspend fun <T : Any> emit(key: EventKey<T>, payload: T) {
        listeners[key]?.forEach {
            it(payload)
        }

        listenersOnce[key]?.onEach {
            it(payload)
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
}
