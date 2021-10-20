package com.astronaut.server.socket.impl.udp

import com.astronaut.server.socket.ClientSocket
import com.astronaut.server.socket.ServerSocket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.InetSocketAddress

class UDPServerSocket(
    appScope: CoroutineScope,
    // Just because Ktor itself has a strange lock inside,
    // in single thread it looks like blocking operation
    // with package send waiting, but in UDP we dont need to track it
    senderScope: CoroutineScope,
    private val pool: UDPClientsPool,
    hostname: String,
    port: Int
): ServerSocket {
    private val socket: BoundDatagramSocket =
        aSocket(ActorSelectorManager(appScope.coroutineContext))
            .udp()
            .bind(InetSocketAddress(hostname, port)) {
                this.reusePort = true
            }

    init {
        appScope.launch {
            pool.setSendDelegate {
                senderScope.launch {
                    socket.outgoing.send(it)
                }
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