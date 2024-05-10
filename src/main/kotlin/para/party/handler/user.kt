package para.party.handler

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull
import para.party.db.db
import para.party.entity.User
import para.party.entity.users
import para.party.model.RegisterRequest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.registerHandler(unused: Unit) {
    val requestBody = call.receive<RegisterRequest>()
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

suspend fun PipelineContext<Unit, ApplicationCall>.getUserHandler(unused: Unit) {
    val id = call.parameters["id"] ?: throw InvalidBodyException("?")
    val result = db.users.firstOrNull { it.id eq id } ?: throw Exception("找不到")
    call.respond(result)
}
