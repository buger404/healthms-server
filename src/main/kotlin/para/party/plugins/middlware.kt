package para.party.plugins

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import para.party.serialization.jsonMapper

fun Application.registerMiddleware() {
    install(ContentNegotiation) {
        val converter = JacksonConverter(jsonMapper)
        register(ContentType.Application.Json, converter)
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError, mapOf(
                    "errors" to listOf(cause.message),
                )
            )
            logError(call, cause)
        }
    }
//    intercept(ApplicationCallPipeline.Call) {
//        proceed()
//        val originalResponse = call.response.responseType
//        if (call.response.status() == HttpStatusCode.OK && originalResponse != null) {
//            val wrappedResponse = mapOf("data" to originalResponse)
//            call.respond(wrappedResponse)
//        }
//    }
}
