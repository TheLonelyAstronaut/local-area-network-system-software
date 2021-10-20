package com.astronaut.server.socket.impl.udp

import com.astronaut.server.socket.ClientSocket
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class UDPClientSocket(
    private val address: NetworkAddress,
    private var readData: ByteReadPacket?,
    private val onWrite: suspend (data: Datagram) -> Unit,
    private val onClose: () -> Unit,
): ClientSocket {
    private var isActive: Boolean = true

    @OptIn(ExperimentalTime::class)
    override suspend fun readString(): String? {
        while (readData == null) {
            delay(Duration.nanoseconds(1))

            if(!isActive) {
                return null
            }
        }

        val text = readData!!.readText()
        readData = null

        return text
    }

    override suspend fun writeString(data: String): Boolean {
        onWrite(Datagram(ByteReadPacket((data + "\r\n").encodeToByteArray()), address))

        return true
    }

    override suspend fun writeByteArray(data: ByteArray) {
        //println(data.size)
        onWrite(Datagram(ByteReadPacket(data), address))
    }

    override suspend fun close() {
        isActive = false
        onClose()
    }

    fun setReadData(data: ByteReadPacket) {
        readData = data
    }
}