package com.astronaut.server

import com.astronaut.server.di.DIRoot

fun main() {
    DIRoot.getConfigInstance().configure(
        isMultithreaded = false,
        isSynchronous = false,
    )

    DIRoot.getServerInstance().start()
}

/*
val local = InetSocketAddress("0.0.0.0", 2324);
val repo = FileRepositoryImpl()

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
 */

