package com.astronaut.server.server.impl

import com.astronaut.server.config.ServerConfig
import com.astronaut.server.controller.BaseController
import com.astronaut.server.controller.FileController
import com.astronaut.server.server.Server
import com.astronaut.server.server.ServerSocketWrapper
import com.astronaut.server.socket.ClientSocket
import com.astronaut.server.utils.Events
import kotlinx.coroutines.launch

class ServerImpl(
    private val serverSocket: ServerSocketWrapper,
    private val config: ServerConfig,
    private val baseController: BaseController,
    private val fileController: FileController
): Server {
    override fun start() {
        config.appScope.launch {
            while (true) {
                val client = serverSocket.accept()

                if(config.isSynchronous) {
                    launchSync(client)
                } else {
                    launchAsync(client)
                }
            }
        }
    }

    private suspend fun launchAsync(socket: ClientSocket) {
        config.appScope.launch {
            launchSync(socket)
        }
    }

    private suspend fun launchSync(socket: ClientSocket) {
        try {
            while (true) {
                val data = socket.readString();

                if(data.isNullOrEmpty()) {
                    print("Connection closed")

                    break
                }

                when(val event = Events.parseFromClientString(data)) {
                    is Events.ECHO,
                    is Events.UNKNOWN,
                    is Events.TIME,
                    is Events.CLOSE -> {
                        baseController.resolve(socket, event)
                    }
                    is Events.DOWNLOAD -> {
                        fileController.download(socket, event)
                    }
                    is Events.UPLOAD -> {
                        fileController.upload(socket, event)
                    }
                }
            }

        } catch (e: Throwable) {
            e.printStackTrace()
            socket.close()
        }
    }
}