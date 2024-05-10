package para.party

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import para.party.plugins.registerMiddleware
import para.party.plugins.registerRoute

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    registerMiddleware()
    registerRoute()
}
