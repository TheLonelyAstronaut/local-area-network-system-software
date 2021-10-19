package com.astronaut

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress

fun main() {
    runBlocking {
        val server = aSocket(ActorSelectorManager(this.coroutineContext)).tcp().bind(InetSocketAddress("0.0.0.0", 2323))
        println("Started echo telnet server at ${server.localAddress}")
        Test.start()

        while (true) {
            println("HERE")
            val socket = server.accept()

            println("Socket accepted: ${socket.remoteAddress}")

            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = true)

            try {
                while (true) {
                    val line = input.readUTF8Line(Int.MAX_VALUE)

                    println("${socket.remoteAddress}: $line")

                    val data = "$line\r\n"

                    output.writeAvailable(data.encodeToByteArray(), 0, data.length)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                socket.close()
            }
        }
    }
}
