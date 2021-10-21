package com.astronaut.server.config

import com.astronaut.server.utils.ServerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

class ServerConfig {
    var appScope: CoroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        private set

    var isMultithreaded: Boolean = false
        private set

    var serverProtocol: ServerProtocol = ServerProtocol.TCP
        private set

    var hostname: String = "0.0.0.0"
        private set

    var port: Int = 2323
        private set

    var isSynchronous: Boolean = false
        private set

    fun configure(
        protocol: ServerProtocol,
        isMultithreaded: Boolean = false,
        isSynchronous: Boolean = false
    ) {
        serverProtocol = protocol
        this.isSynchronous = isSynchronous
        this.isMultithreaded = isMultithreaded

        when(protocol) {
            ServerProtocol.TCP -> {
                appScope = CoroutineScope(
                    (if(isMultithreaded)
                        Executors.newCachedThreadPool()
                    else
                        Executors.newSingleThreadExecutor())
                        .asCoroutineDispatcher()
                )
            }
            ServerProtocol.UDP -> {
                appScope = CoroutineScope(
                    (if(isMultithreaded)
                        Executors.newCachedThreadPool()
                    else
                        Executors.newSingleThreadExecutor())
                        .asCoroutineDispatcher()
                )
            }
        }
    }
}