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

    override suspend fun readByteArray(data: ByteArray): Int? {
        var size: Int? = readChannel.readAvailable(data)

        if(size == -1) size = null

        return size
    }

    override suspend fun writeString(data: String) {
        writeChannel.writeAvailable((data + "\r\n").encodeToByteArray())
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