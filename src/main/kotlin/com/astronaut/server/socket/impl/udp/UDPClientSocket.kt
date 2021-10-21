package com.astronaut.server.socket.impl.udp

import com.astronaut.server.socket.ClientSocket
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class UDPClientSocket(
    private val address: NetworkAddress,
    private var readData: ByteReadPacket?,
    private val onWrite: suspend (data: Datagram) -> Unit,
    private val onClose: () -> Unit,
): ClientSocket {
    private var isActive: Boolean = true
    private val mutex = Mutex()

    override suspend fun readString(): String? {
        // If data to read is null - lock until it will be unlocked in setReadData method
        if(readData == null) {
            if(!isActive) {
                return null
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

        val text = readData?.readText()
        readData = null

        return text
    }

    override suspend fun writeString(data: String): Boolean {
        onWrite(Datagram(ByteReadPacket((data + "\r\n").encodeToByteArray()), address))

        return true
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

        mutex.unlock()
    }
}