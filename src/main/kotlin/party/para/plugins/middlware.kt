package party.para.plugins

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import party.para.serialization.jsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

fun Application.registerMiddleware() {
    install(ContentNegotiation) {
        val converter = JacksonConverter(jsonMapper)
        register(ContentType.Application.Json, converter)
        jackson {
            registerModule(JavaTimeModule())
        }
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
