package com.astronaut.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.util.*

val CHUNK_SIZE = 1024

fun main() {
    runBlocking {
        val address = InetSocketAddress("0.0.0.0", 2323);
        val local = InetSocketAddress("0.0.0.0", 2525);

        val socket =
            aSocket(ActorSelectorManager(coroutineContext))
                .tcp()
                .connect(
                    remoteAddress = address,
                    // localAddress = local,
                    configure = {
                        this.reusePort = true
                    }
                )


        val channel = socket.openReadChannel()
        val channel2 = socket.openWriteChannel(autoFlush = true)

        while (true) {
            print("Enter file name: ")
            val line = readLine() ?: ""

            //socket.send(Datagram(ByteReadPacket(line.encodeToByteArray()), address))
            //print(socket.incoming.receive().packet.readText())
            channel2.writeAvailable("DOWNLOAD $line\r\n".encodeToByteArray())

            writeToFile(line) {
                channel.readAvailable(it)
            }
        }

        /*while (true) {
            print("Enter file name: ")
            val line = readLine() ?: ""
            val byteArray = ByteArray(CHUNK_SIZE)

            socket.send(Datagram(ByteReadPacket("DOWNLOAD $line".encodeToByteArray()), address))

            writeToFile(line) {
                socket.receive().packet.readAvailable(it)
            }
        }*/
    }
}

suspend fun writeToFile(name: String, receiveChunk: suspend (ByteArray) -> Int) {
    val outputStream = FileOutputStream("data/client/$name")

    //println(line)
    var actualSize: Int
    var commonSize = 0
    val start = Date().time

    do {
        val byteArray = ByteArray(CHUNK_SIZE)

        actualSize = receiveChunk(byteArray)

        if(actualSize != -1) {
            commonSize += actualSize

            outputStream.write(byteArray, 0, actualSize)

            if(actualSize != CHUNK_SIZE) {
                actualSize = -1
            }
        }
    } while (actualSize != -1)

    println(Date().time - start)

    outputStream.close()
}