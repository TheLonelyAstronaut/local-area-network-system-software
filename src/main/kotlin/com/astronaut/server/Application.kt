package com.astronaut.server
import com.astronaut.server.di.DIRoot
import com.astronaut.server.utils.ServerProtocol

fun main() {
    DIRoot.getConfigInstance().configure(
        protocol = ServerProtocol.TCP,
        isMultithreaded = true,
        isSynchronous = false
    )

    DIRoot.getServerInstance().start()
}

// Simple examples
/*
/*scope.launch {
        val server = aSocket(ActorSelectorManager(this.coroutineContext))
            .udp()
            .bind(InetSocketAddress("0.0.0.0", 2323))

        println("Started echo telnet server at ${server.localAddress}")

        while (true) {
            println("Waiting for connections")

            val socket = server.incoming.receive()

            val data = "${socket.packet.readText()}\r\n"


            println("Staring")
            val pack1 = Datagram(ByteReadPacket(data.encodeToByteArray()), address = socket.address)
            val pack2 = Datagram(ByteReadPacket(data.encodeToByteArray()), address = socket.address)
            val pack3 = Datagram(ByteReadPacket(data.encodeToByteArray()), address = socket.address)
            val pack4 = Datagram(ByteReadPacket(data.encodeToByteArray()), address = socket.address)
            val pack5 = Datagram(ByteReadPacket(data.encodeToByteArray()), address = socket.address)
            println("Creating")

            server.outgoing.send(pack1)

            server.outgoing.send(pack2)

            server.outgoing.send(pack3)

            server.outgoing.send(pack4)

            server.outgoing.send(pack5)

            println("HERE")

        }
    }*/

    /*scope.launch {
        val server = aSocket(ActorSelectorManager(this.coroutineContext))
            .tcp()
            .bind(InetSocketAddress("0.0.0.0", 2323))


        println("Started echo telnet server at ${server.localAddress}")

        while (true) {
            val socket = server.accept()

            launch {
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
    }*/
*/
