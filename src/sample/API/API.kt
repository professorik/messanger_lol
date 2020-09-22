package sample.API

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.lang.Math.random
import java.lang.RuntimeException
import java.net.Socket
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread
import kotlin.math.floor


class AppSocket: EventEmitter<JSONObject>() {
    val socket = Socket("127.0.0.1", 8080)
    val thread: Thread
    var requests = 0

    init {
        thread = thread(true) {
            while (true) {
                val lengthArray = socket.readBlocking(4)
                var length = 0
                for (i in 0..3) length += lengthArray[i] shl (i*8)
                val string = String(socket.readBlocking(length).map { it.toByte() }.toByteArray(), StandardCharsets.UTF_8)
                val data = JSONParser().parse(string) as JSONObject
                emit(data)
            }
        }
    }

    @Deprecated("Use the request method", ReplaceWith("request()"))
    fun send(data: JSONObject) {
        val byteArray = data.toJSONString().toByteArray()
        val byteArrayLength = byteArray.size
        for (i in 0..3) socket.getOutputStream().write((byteArrayLength shr (i*8)) and 0xFF)
        socket.getOutputStream().write(byteArray)
    }

    fun request(data: JSONObject): EventEmitter<JSONObject> {
        val requestId = requests++
        data["_request_id"] = requestId
        val request = EventEmitter<JSONObject>()
        addListener {
            val received = it["_request_id"] ?: return@addListener
            if (received is Long && received.toInt() == requestId) {
                request.emit(it)
            }
        }
        send(data)
        return request
    }

    fun close() {
        thread.stop()
        socket.close()
    }
}

