package com.astronaut.server.socket.impl.udp

import com.astronaut.server.socket.ClientSocket
import com.astronaut.server.socket.ServerSocket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress

class UDPServerSocket(
    appScope: CoroutineScope,
    private val pool: UDPClientsPool,
    hostname: String,
    port: Int
): ServerSocket {
    private val socket: BoundDatagramSocket =
        aSocket(ActorSelectorManager(appScope.coroutineContext))
            .udp()
            .bind(InetSocketAddress(hostname, port))

    init {
        appScope.launch(start = CoroutineStart.UNDISPATCHED) {
            pool.setSendDelegate {
                socket.outgoing.send(it)
            }

            while (true) {
                val incomingPackage = socket.incoming.receive()

                pool.addOrUpdateClient(incomingPackage)
            }
        }
    }

    override suspend fun accept(): ClientSocket {
        return pool.getNewClient()
    }
}