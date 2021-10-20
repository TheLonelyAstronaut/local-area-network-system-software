package com.astronaut.server.socket.impl.tcp

import com.astronaut.server.socket.ClientSocket
import io.ktor.network.sockets.*
import io.ktor.utils.io.*

class TCPClientSocket(
    private val raw: Socket
): ClientSocket {
    private val readChannel = raw.openReadChannel()
    private val writeChannel = raw.openWriteChannel(autoFlush = true)

    override suspend fun readString(): String? {
        return readChannel.readUTF8Line(Int.MAX_VALUE)
    }

    override suspend fun writeString(data: String): Boolean {
        return try {
            writeChannel.writeAvailable((data + "\r\n").encodeToByteArray())
            true
        } catch (e: Throwable) {
            close()

            false
        }
    }

    override suspend fun writeByteArray(data: ByteArray) {
        writeChannel.writeFully(data)
    }

    override suspend fun close() {
        runCatching {
            raw.close()
        }
    }
}