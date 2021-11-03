package com.astronaut.client

import com.astronaut.common.utils.Events
import com.astronaut.common.utils.WindowingHandler
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*

class UDPClientSocket(
    private val socket: ConnectedDatagramSocket,
    private val address: NetworkAddress
): WindowingHandler() {
    suspend fun sendEvent(e: Events) {
        send(e.toString().encodeToByteArray())
    }

    suspend fun receiveEvent(): Events {
        return Events.parseFromClientString(receiveString())
    }

    suspend fun receiveString(): String {
        return String(receive())
    }

    suspend fun receiveByteArray(buffer: ByteArray): Int {
        return try {
            receive().copyInto(buffer)
            buffer.size
        } catch (e: Throwable) {
            e.printStackTrace()
            -1
        }
    }

    suspend fun sendRawByteArray(data: ByteArray, forceListen: Boolean = false) {
        send(data, forceListen)
    }

    override suspend fun sendByteArray(byteArray: ByteArray) {
        socket.send(Datagram(ByteReadPacket(byteArray), address))
    }

    override suspend fun receiveByteReadPacket(): ByteReadPacket {
        return socket.receive().packet
    }
}