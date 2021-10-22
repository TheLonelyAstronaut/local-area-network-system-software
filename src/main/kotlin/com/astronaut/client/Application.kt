package com.astronaut.client

import com.astronaut.common.repository.impl.FileRepositoryImpl
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.io.FileOutputStream
import java.net.InetSocketAddress
import kotlin.coroutines.CoroutineContext

val repo = FileRepositoryImpl()

val address = InetSocketAddress("192.168.31.143", 2323);
val local = InetSocketAddress("0.0.0.0", 2528);

fun main() {
    runBlocking {
        downloadFileWithTCP(coroutineContext)
        //downloadFileWithUDP(coroutineContext)
        //uploadWithTCP(coroutineContext)
        //uploadWithUDP(coroutineContext)
    }
}

suspend fun downloadFileWithTCP(coroutineContext: CoroutineContext) {
    val socket =
        aSocket(ActorSelectorManager(coroutineContext))
            .tcp()
            .connect(
                remoteAddress = address,
            )

    val readChannel = socket.openReadChannel()
    val writeChannel = socket.openWriteChannel(autoFlush = true)

    while (true) {
        print("Enter file name: ")
        val line = readLine() ?: ""

        writeChannel.writeAvailable("DOWNLOAD $line\r\n".encodeToByteArray())

        repo.writeFile("data/client/$line") {
            readChannel.readAvailable(it)
        }
    }
}

suspend fun downloadFileWithUDP(coroutineContext: CoroutineContext) {
    val socket =
        aSocket(ActorSelectorManager(coroutineContext))
            .udp()
            .connect(
                remoteAddress = address,
                localAddress = local
            )

    while (true) {
        print("Enter file name: ")
        val line = readLine() ?: ""

        socket.send(Datagram(ByteReadPacket("DOWNLOAD $line".encodeToByteArray()), address))

        repo.writeFile("data/client/$line") {
            socket.receive().packet.readAvailable(it)
        }
    }
}

suspend fun uploadWithTCP(coroutineContext: CoroutineContext) {
    val socket =
        aSocket(ActorSelectorManager(coroutineContext))
            .tcp()
            .connect(
                remoteAddress = address,
            )

    val writeChannel = socket.openWriteChannel(autoFlush = true)

    while (true) {
        print("Enter file name: ")
        val line = readLine() ?: ""

        writeChannel.writeAvailable("UPLOAD $line\r\n".encodeToByteArray())

        repo.readFile("data/client/$line")
            .collect {
                writeChannel.writeAvailable(it)
            }
    }
}

suspend fun uploadWithUDP(coroutineContext: CoroutineContext) {
    val socket =
        aSocket(ActorSelectorManager(coroutineContext))
            .udp()
            .connect(
                remoteAddress = address,
                localAddress = local
            )

    while (true) {
        print("Enter file name: ")
        val line = readLine() ?: ""

        socket.send(Datagram(ByteReadPacket("UPLOAD $line".encodeToByteArray()), address))

        repo.readFile("data/client/$line")
            .collect {
                //socket.send(Datagram(ByteReadPacket(it), address))
            }
    }
}
