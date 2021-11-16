package com.astronaut.client

import com.astronaut.common.repository.impl.FileRepositoryImpl
import com.astronaut.common.socket.udp.UDPSocket
import com.astronaut.common.socket.udp.runSuspending
import com.astronaut.common.socket.udp.send
import com.astronaut.common.utils.Events
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

val repo = FileRepositoryImpl()

val tcpAddress = InetSocketAddress("192.168.31.143", 2323);
val udpAddress = InetSocketAddress("192.168.31.143", 2324);
val local = InetSocketAddress("0.0.0.0", 2528);
val udpUploadContext = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

fun main() {
    runBlocking {
        //downloadFileWithTCP(coroutineContext)
        downloadFileWithUDP(coroutineContext)
        //uploadWithTCP(coroutineContext)
        //uploadWithUDP(coroutineContext)
        //testWindowHandling(coroutineContext)
    }
}

suspend fun downloadFileWithUDP(coroutineContext: CoroutineContext) {
    val socket = UDPSocket()
    socket.bind(local)

    withContext(coroutineContext) {
        socket.runSuspending()
    }

    while (true) {
        print("Enter file name: ")
        val line = readLine() ?: ""
        val path = "data/client/$line"
        val size = repo.getFileSize(path)

        socket.send(Events.DOWNLOAD(line, size).toString().toByteArray(), udpAddress)

        /*repo.writeFile(path, 0, size) {
            socket.receive().data.array().copyInto(it)
            it.lastIndex + 1
        }*/
    }
}

suspend fun testWindowHandling(coroutineContext: CoroutineContext) {
    val socket =
        aSocket(ActorSelectorManager(coroutineContext))
            .udp()
            .connect(
                remoteAddress = udpAddress,
                localAddress = local
            )

    val socketWrapper = UDPClientSocket(socket, udpAddress)

    while (true) {
        print("Press enter to send TIME event")
        readLine()

        socketWrapper.sendEvent(Events.TIME)
        val response = socketWrapper.receiveString()

        println(response)
    }
}

suspend fun downloadFileWithTCP(coroutineContext: CoroutineContext) {
    val socket =
        aSocket(ActorSelectorManager(coroutineContext))
            .tcp()
            .connect(
                remoteAddress = tcpAddress,
            )

    val readChannel = socket.openReadChannel()
    val writeChannel = socket.openWriteChannel(autoFlush = true)

    while (true) {
        print("Enter file name: ")
        val line = readLine() ?: ""
        val path = "data/client/$line"

        val size = repo.getFileSize(path)

        writeChannel.writeAvailable(Events.DOWNLOAD(line, size).toString().encodeToByteArray())

        when(val command = Events.parseFromClientString(readChannel.readUTF8Line(Int.MAX_VALUE) ?: "")) {
            is Events.OK -> {
                if(size == command.payload && command.payload.toInt() != 0) {
                    println("Already downloaded!")

                    continue
                }

                if(command.payload.toInt() == 0) {
                    println("No such file on server!")

                    continue
                }

                try {
                    repo.writeFile(path, command.payload, size) {
                        readChannel.readAvailable(it)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            else -> {
                println(command)
                continue
            }
        }
    }
}

/*suspend fun downloadFileWithUDP(coroutineContext: CoroutineContext) {
    val socket =
        aSocket(ActorSelectorManager(coroutineContext))
            .udp()
            .connect(
                remoteAddress = udpAddress,
                localAddress = local
            )

    val socketWrapper = UDPClientSocket(socket, udpAddress)

    while (true) {
        print("Enter file name: ")
        val line = readLine() ?: ""
        val path = "data/client/$line"

        val size = repo.getFileSize(path)

        socketWrapper.sendEvent(Events.DOWNLOAD(line, size))

        when(val command = socketWrapper.receiveEvent()) {
            is Events.OK -> {
                if(size == command.payload && command.payload.toInt() != 0) {
                    println("Already downloaded!")

                    continue
                }

                if(command.payload.toInt() == 0) {
                    println("No such file on server!")

                    continue
                }

                try {
                    withContext(udpUploadContext.coroutineContext) {
                        repo.writeFile(path, command.payload, size) {
                            socketWrapper.receiveByteArray(it)
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            else -> {
                println(command)
                continue
            }
        }
    }
}*/

suspend fun uploadWithTCP(coroutineContext: CoroutineContext) {
    val socket =
        aSocket(ActorSelectorManager(coroutineContext))
            .tcp()
            .connect(
                remoteAddress = tcpAddress,
            )

    val readChannel = socket.openReadChannel()
    val writeChannel = socket.openWriteChannel(autoFlush = true)

    while (true) {
        print("Enter file name: ")
        val line = readLine() ?: ""
        val path = "data/client/$line"

        val size = repo.getFileSize(path)

        writeChannel.writeAvailable(Events.UPLOAD(line, size).toString().encodeToByteArray())

        when(val command = Events.parseFromClientString(readChannel.readUTF8Line(Int.MAX_VALUE) ?: "")) {
            is Events.OK -> {
                println("$size ${command.payload}")

                if(size == command.payload) {
                    println("Already uploaded!")
                    writeChannel.writeAvailable(Events.END().toString().encodeToByteArray())

                    continue
                } else {
                    writeChannel.writeAvailable(Events.START().toString().encodeToByteArray())
                }

                var accumulator = 0;

                repo.readFile(path, offset = command.payload)
                    .onCompletion {
                        //socket.close() // <-- Uncomment this to test connection interruption
                        println(accumulator)
                    }
                    .collect {
                        //accumulator += it.size // <-- Uncomment this to test connection interruption

                        if(accumulator <= 2000000) {
                           writeChannel.writeFully(it.data)
                        }
                    }
            }
            else -> {
                println(command)
                continue
            }
        }
    }
}

suspend fun uploadWithUDP(coroutineContext: CoroutineContext) {
    val socket =
        aSocket(ActorSelectorManager(coroutineContext))
            .udp()
            .connect(
                remoteAddress = udpAddress,
                localAddress = local
            )

    val socketWrapper = UDPClientSocket(socket, udpAddress)

    while (true) {
        print("Enter file name: ")
        val line = readLine() ?: ""
        val path = "data/client/$line"

        val size = repo.getFileSize(path)

        socketWrapper.sendEvent(Events.UPLOAD(line, size))

        when(val command = socketWrapper.receiveEvent()) {
            is Events.OK -> {
                if(size == command.payload) {
                    println("Already uploaded!")
                    socketWrapper.sendEvent(Events.END())

                    continue
                } else {
                    socketWrapper.sendEvent(Events.START())
                }

                withContext(udpUploadContext.coroutineContext) {
                    repo.readFile(path, offset = command.payload)
                        .collect {
                            socketWrapper.sendRawByteArray(it.data.clone(), it.isEnd)
                        }
                }
            }
            else -> {
                println(command)
                continue
            }
        }
    }
}
