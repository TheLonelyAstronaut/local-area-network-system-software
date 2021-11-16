package com.astronaut.server

import com.astronaut.common.repository.impl.CHUNK_SIZE
import com.astronaut.common.repository.impl.FileRepositoryImpl
import com.astronaut.common.socket.udp.UDPSocket
import com.astronaut.common.socket.udp.runSuspending
import com.astronaut.common.socket.udp.send
import com.astronaut.common.utils.Events
import com.astronaut.common.utils.getUnifiedString
import com.astronaut.common.utils.toByteArray
import com.astronaut.common.utils.toEvent
import com.astronaut.server.di.DIRoot
import com.astronaut.server.utils.ServerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.util.concurrent.Executors

val local = InetSocketAddress("0.0.0.0", 2324);
val repo = FileRepositoryImpl()

fun main() {
    /*DIRoot.getConfigInstance().configure(
        protocol = ServerProtocol.TCP,
        isMultithreaded = false,
        isSynchronous = true,
    )

    DIRoot.getServerInstance().start()*/
    val socket = UDPSocket(mtuBytes = CHUNK_SIZE, windowSizeBytes = CHUNK_SIZE * 100, congestionControlTimeoutMs = 1)
    socket.bind(local)
    val context = Executors.newCachedThreadPool().asCoroutineDispatcher()

    CoroutineScope(context).launch {
        launch { socket.runSuspending() }

        while (true) {
            val received = socket.receive()
            val event = received.data.toByteArray().getUnifiedString().toEvent()

            if(event is Events.DOWNLOAD) {
                val size = repo.getFileSize("data/server/${event.filename}");

                socket.send(Events.OK(size).toString().encodeToByteArray(), received.address)

                repo.readFile("data/server/${event.filename}", 0)
                    .collect {
                        socket.send(it.data, received.address)
                    }
            }
        }
    }
}
