package sample.API

import java.lang.Exception
import java.lang.RuntimeException
import java.net.Socket
import java.util.concurrent.CountDownLatch

fun Socket.readBlocking(length: Int): IntArray {
    val result = IntArray(length)
    var i = 0
    while (i < length) {
        if (isClosed) throw RuntimeException("The socket is closed")
        result[i] = getInputStream().read()
        if (result[i] == -1) throw RuntimeException("No more data available")
        i++
    }
    return result
}


open class EventEmitter<T> {
    protected var listeners: MutableList<(T) -> Unit> = ArrayList()

    fun addListener(listener: (T) -> Unit) {
        listeners.add(listener)
    }
    fun removeListener(listener: (T) -> Unit) {
        listeners = listeners.filter { it !== listener }.toMutableList()
    }
    fun emit(event: T) {
        for (i in 0 until listeners.size) {
            val it = listeners[i]
            try {
                it(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun await(): T {
        val latch = CountDownLatch(1)
        var result: T? = null
        addListener { result = it; latch.countDown() }
        latch.await()
        return result!!
    }
}
