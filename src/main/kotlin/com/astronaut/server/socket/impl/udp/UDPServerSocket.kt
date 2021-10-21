package com.astronaut.server.socket.impl.udp

import com.astronaut.server.socket.ClientSocket
import com.astronaut.server.socket.ServerSocket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import java.net.InetSocketAddress
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

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
        appScope.launch {
            pool.setSendDelegate {
                socket.outgoing.send(it)
            }

            socket.incoming.consume {
                this.receiveAsFlow()
                    .collect {
                        pool.addOrUpdateClient(it)
                    }
            }
        }
    }

    override suspend fun accept(): ClientSocket {
        return pool.getNewClient()
    }
}