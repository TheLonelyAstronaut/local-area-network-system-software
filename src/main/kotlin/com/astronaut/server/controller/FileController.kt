package com.astronaut.server.controller

import com.astronaut.server.socket.ClientSocket
import com.astronaut.server.utils.Events

interface FileController {
    fun upload(socket: ClientSocket, event: Events)
    fun download(socket: ClientSocket, event: Events)
}