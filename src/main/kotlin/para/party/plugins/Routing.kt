package para.party.plugins

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.netty.util.internal.logging.Slf4JLoggerFactory
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.toList
import org.slf4j.LoggerFactory
import para.party.db.db
import para.party.entity.User
import para.party.entity.users
import para.party.model.RegisterRequest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

fun Application.configureRouting() {
    val logger = LoggerFactory.getLogger(this::class.java)


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

        get("/users/{id}") {
            val id = call.parameters["id"] ?: throw InvalidBodyException("?")
            // val result = db.users.firstOrNull() { it.id eq id } ?: throw Exception("找不到")
            // val result = db.users.filter { it.id eq id }.firstOrNull() ?: throw Exception("找不到")
            // 不能使用上面那个，因为 sinodb 还支持 limit 0, 1;

            val result = db.users.filter { it.id eq id }.toList().firstOrNull() ?: throw Exception("找不到")
            // 得先查出来再取 first
            call.respond(result)
        }
    }
}
