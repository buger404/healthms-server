package para.party.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.entity.add
import para.party.db.db
import para.party.entity.User
import para.party.entity.users
import para.party.model.RegisterRequest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

fun Application.configureRouting() {
    install(AutoHeadResponse)
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        post("/register") {
            val requestBody = call.receive<RegisterRequest>();
            if (requestBody.password.isNullOrBlank() || (requestBody.password?.length ?: 0) < 6) {
                throw InvalidBodyException("?")
            }

            val user = User().apply {
                id = UUID.randomUUID().toString()
                username = requestBody.username!!
                password = requestBody.password!!
                createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                updatedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            }
            db.users.add(user)

            call.respond(user)
        }
    }
}
