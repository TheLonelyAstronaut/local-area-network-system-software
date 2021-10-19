package com.astronaut

import org.kodein.di.*

class Lol {
    fun meh() {
        println("Meh")
    }
}

val appDI = DI {
    bind<Lol>() with singleton { Lol() }
}

object Test: DIAware {
    override val di: DI
        get() = appDI

    private val lol by instance<Lol>()

    fun start() {
        println(lol.meh())
    }
}