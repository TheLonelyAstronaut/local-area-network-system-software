package com.astronaut.server.socket.impl.udp

import io.ktor.network.sockets.*
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class UDPClientsPool {
    private val users: MutableMap<String, UDPClientSocket> = mutableMapOf()
    private val newUsers: MutableMap<String, UDPClientSocket> = mutableMapOf()

    private lateinit var sendDelegate: suspend (data: Datagram) -> Unit

    fun addOrUpdateClient(data: Datagram) {
        if(users.contains(data.address.toString())) {
            users.getValue(data.address.toString()).setReadData(data.packet)
        } else {
            val connection = UDPClientSocket(
                data.address,
                data.packet,
                sendDelegate
            ) {
                data.packet.close()
                users.remove(data.address.toString())
            }

            newUsers[data.address.toString()] = connection
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getNewClient(): UDPClientSocket {
        while (newUsers.isEmpty()) {
            delay(Duration.Companion.nanoseconds(1))
        }

        val user = newUsers.keys.elementAt(0);
        val udp = newUsers.getValue(user)

        users[user] = udp
        newUsers.remove(user)

        return udp
    }

    fun setSendDelegate(delegate: suspend (data: Datagram) -> Unit) {
        sendDelegate = delegate
    }
}
