package com.astronaut.server.socket.impl.udp

import com.astronaut.common.utils.WindowingHandler
import com.astronaut.server.socket.ClientSocket
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.coroutineContext

class UDPClientSocket(
    private val address: NetworkAddress,
    private var readData: ByteReadPacket?,
    private val onWrite: suspend (data: Datagram) -> Unit,
    private val onClose: () -> Unit,
): ClientSocket, WindowingHandler() {

    private var isActive: Boolean = true
    private val mutex = Mutex()
    private var isTimeoutEnabled = false

    private suspend fun waitBeforeRead() {
        try {
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
        } catch (e: Throwable) {
            if(mutex.isLocked) {
                mutex.unlock()
            }
        }
    }

    override suspend fun sendByteArray(byteArray: ByteArray) {
        internalWriteByteArray(byteArray)
    }

    override suspend fun receiveByteReadPacket(): ByteReadPacket {
        if(isTimeoutEnabled) {
            var result = withTimeoutOrNull(5) {
                internalReadByteArray()
            }

            if(result == null) {
                result = ByteReadPacket(byteArrayOf())
            }

            return result
        } else {
            return internalReadByteArray()
        }
    }

    private suspend fun internalReadByteArray(): ByteReadPacket {
        waitBeforeRead()

        if(!isActive && readData == null) {
            return ByteReadPacket(byteArrayOf())
        }

        val copy = readData?.copy() ?: ByteReadPacket(byteArrayOf())
        readData = null

        return copy
    }

    private suspend fun internalWriteByteArray(data: ByteArray) {
        onWrite(Datagram(ByteReadPacket(data), address))
    }

    override suspend fun readString(): String? {
        isTimeoutEnabled = false
        val data = receive()
        isTimeoutEnabled = true

        return String(data)
    }

    override suspend fun readByteArray(data: ByteArray): Int? {
        receive().copyInto(data)

        return data.size
    }

    override suspend fun writeString(data: String) {
        send(data.encodeToByteArray())
    }

    override suspend fun writeByteArray(data: ByteArray) {
        send(data)
    }

    override suspend fun close() {
        isActive = false
        mutex.unlock()
        onClose()
    }

    override suspend fun forceApproval() {
        sendApproval()
    }

    fun setReadData(data: ByteReadPacket) {
        readData = data

        try {
            if(mutex.isLocked) mutex.unlock()
        } catch (e: Throwable) {

        }
    }
}