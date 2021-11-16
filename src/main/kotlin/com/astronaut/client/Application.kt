package com.astronaut.client

import com.astronaut.common.repository.impl.CHUNK_SIZE
import com.astronaut.common.repository.impl.FileRepositoryImpl
import com.astronaut.common.socket.udp.UDPSocket
import com.astronaut.common.socket.udp.runSuspending
import com.astronaut.common.socket.udp.send
import com.astronaut.common.utils.Events
import com.astronaut.common.utils.getUnifiedString
import com.astronaut.common.utils.toByteArray
import com.astronaut.common.utils.toEvent
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

val repo = FileRepositoryImpl()

val tcpAddress = InetSocketAddress("192.168.31.143", 2323);
val udpAddress = InetSocketAddress("192.168.31.69", 2324);
val local = InetSocketAddress("0.0.0.0", 2828);
val udpUploadContext = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

fun main() {
    //downloadFileWithUDP()
   runBlocking {
        //downloadFileWithTCP(coroutineContext)
        //downloadFileWithUDP(coroutineContext)
        //uploadWithTCP(coroutineContext)
        uploadWithUDP(coroutineContext)
        //testWindowHandling(coroutineContext)
   }
}

 /*fun downloadFileWithUDP() {
    val socket = UDPSocket(mtuBytes = CHUNK_SIZE,
        windowSizeBytes = CHUNK_SIZE * 100,
        congestionControlTimeoutMs = 1,
    )
    socket.bind(local)
    val context = Executors.newCachedThreadPool().asCoroutineDispatcher()

    CoroutineScope(context).launch {
        launch { socket.runSuspending() }

        while (true) {
            print("Enter file name: ")
            val line = readLine() ?: ""
            val path = "data/client/$line"
            val size = repo.getFileSize(path)

            socket.send(Events.DOWNLOAD(line, size).toString().toByteArray(), udpAddress)
            val ok = socket.receive().data.toByteArray().getUnifiedString().toEvent()

            if(ok is Events.OK) {
                repo.writeFile(path, ok.payload, size) {
                    val data = socket.receive().data.toByteArray()
                    data.copyInto(it)

                    data.size
                }
            } else {
                println(ok)
            }
        }
    }
}*/

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

suspend fun downloadFileWithUDP(coroutineContext: CoroutineContext) {
    val socket = UDPSocket(mtuBytes = CHUNK_SIZE,
        windowSizeBytes = CHUNK_SIZE * 100,
        congestionControlTimeoutMs = 1,
    )

    socket.bind(local)

    val socketWrapper = UDPClientSocket(socket, udpAddress)

    CoroutineScope(coroutineContext).launch {
        launch { socketWrapper.runSuspending() }

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
                        repo.writeFile(path, command.payload, size) {
                            socketWrapper.receiveByteArray(it)
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
}

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
    val socket = UDPSocket(mtuBytes = CHUNK_SIZE,
            windowSizeBytes = CHUNK_SIZE * 100,
            congestionControlTimeoutMs = 1,
    )

    socket.bind(local)

    val socketWrapper = UDPClientSocket(socket, udpAddress)

    CoroutineScope(coroutineContext).launch {
        launch { socketWrapper.runSuspending() }

        while (true) {
            print("Enter file name: ")
            val line = readLine() ?: ""
            val path = "data/client/$line"

            val size = repo.getFileSize(path)

            socketWrapper.sendEvent(Events.UPLOAD(line, size))

            when (val command = socketWrapper.receiveEvent()) {
                is Events.OK -> {
                    if (size == command.payload) {
                        println("Already uploaded!")
                        socketWrapper.sendEvent(Events.END())

                        continue
                    } else {
                        socketWrapper.sendEvent(Events.START())
                    }

                    withContext(udpUploadContext.coroutineContext) {
                        repo.readFile(path, offset = command.payload)
                            .collect {
                                socketWrapper.sendByteArray(it.data.clone())
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
}
