package com.astronaut.server.socket.impl.udp

import com.astronaut.common.utils.WindowingHandler
import com.astronaut.server.socket.ClientSocket
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.sync.Mutex

class UDPClientSocket(
    private val address: NetworkAddress,
    private var readData: ByteReadPacket?,
    private val onWrite: suspend (data: Datagram) -> Unit,
    private val onClose: () -> Unit,
): ClientSocket, WindowingHandler() {

    private var isActive: Boolean = true
    private val mutex = Mutex()

    private suspend fun waitBeforeRead() {
        // If data to read is null - lock until it will be unlocked in setReadData method
        if(readData == null) {
            if(!isActive) {
                return
            }

            // Dont wait for unlock here if its locked (it means that readString was called earlier than setReadData)
            if(!mutex.isLocked) {
                mutex.lock()
            }
        } else {
            // If data exists - unlock mutex
            if(mutex.isLocked) {
                mutex.unlock()
            }
        }

        // Lock mutex and read from datagram if data exists, wait for unlock if data is null
        mutex.lock()
    }

    override suspend fun readString(): String? {
        waitBeforeRead()

        if(!isActive) {
            return null
        }

        val text = readData?.readText()
        readData = null

        return text
    }

    override suspend fun readByteArray(data: ByteArray): Int? {
        waitBeforeRead()

        if(!isActive) {
            return null
        }

        val size = readData?.readAvailable(data)
        readData = null

        return size
    }

    override suspend fun writeString(data: String) {
        onWrite(Datagram(ByteReadPacket((data + "\r\n").encodeToByteArray()), address))
    }

    override suspend fun writeByteArray(data: ByteArray) {
        onWrite(Datagram(ByteReadPacket(data), address))
    }

    override suspend fun close() {
        isActive = false
        mutex.unlock()
        onClose()
    }

    fun setReadData(data: ByteReadPacket) {
        readData = data

        if(mutex.isLocked) mutex.unlock()
    }
}