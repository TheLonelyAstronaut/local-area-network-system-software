package com.astronaut.server

import com.astronaut.common.socket.udp.UDPSocket
import com.astronaut.server.di.DIRoot
import com.astronaut.server.utils.ServerProtocol
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress

val local = InetSocketAddress("0.0.0.0", 2324);

fun main() {
    /*DIRoot.getConfigInstance().configure(
        protocol = ServerProtocol.TCP,
        isMultithreaded = false,
        isSynchronous = true,
    )

    DIRoot.getServerInstance().start()*/
    val socket = UDPSocket()
    socket.bind(local)

    runBlocking {
        while (true) {
            val received = socket.receive();

            println(String(received.data.array()))
        }
    }
}