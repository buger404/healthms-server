package party.para

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import party.para.plugins.registerMiddleware
import party.para.plugins.registerRoute

fun main() {
    embeddedServer(Netty, port = 9371, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    registerMiddleware()
    registerRoute()
}
