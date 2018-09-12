package horus.events

import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface EventKey<out T : Any>

interface EventEmitter {
    suspend fun <T : Any> emit(key: EventKey<T>, payload: T)

    fun <T : Any> on(key: EventKey<T>, block: (suspend (T) -> Unit))
    fun <T : Any> once(key: EventKey<T>, block: (suspend (T) -> Unit))

    fun clear(key: EventKey<*>? = null)
    fun <T : Any> remove(listener: (suspend (T) -> Unit))
}

fun <T : Any> EventEmitter.emitBlocking(key: EventKey<T>,
                                        payload: T,
                                        coroutineContext: CoroutineContext = EmptyCoroutineContext
) {
    runBlocking(coroutineContext) {
        emit(key, payload)
    }
}
