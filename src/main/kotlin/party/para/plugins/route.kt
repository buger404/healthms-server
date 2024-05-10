package party.para.plugins

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.slf4j.LoggerFactory
import party.para.handler.*
import java.util.*

fun Application.registerRoute() {
    val logger = LoggerFactory.getLogger(this::class.java)

    install(AutoHeadResponse)
    routing {
        get("/") {
            call.respondText("Hello World!")
        }


        post("/register", PipelineContext<Unit, ApplicationCall>::registerHandler)
        get("/users/{id}", PipelineContext<Unit, ApplicationCall>::getUserHandler)
    }
}
