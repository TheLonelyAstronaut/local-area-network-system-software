package com.astronaut.server

import com.astronaut.server.di.DIRoot
import com.astronaut.server.utils.ServerProtocol

fun main() {
    DIRoot.getConfigInstance().configure(
        protocol = ServerProtocol.TCP,
        isMultithreaded = true,
        isSynchronous = false,
    )

    DIRoot.getServerInstance().start()
}
