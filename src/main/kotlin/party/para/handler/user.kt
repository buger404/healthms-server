package party.para.handler

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull
import party.para.db.db
import party.para.entity.User
import party.para.entity.users
import party.para.model.LoginRequest
import party.para.model.LoginResponse
import party.para.model.RegisterRequest
import party.para.model.RegisterResponse
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

object TokenStore {
    val userMap: MutableMap<String, String> = mutableMapOf()
}

suspend fun checkUsername(username : String) : Boolean{
    return !username.any { !(it.isDigit() || it.isLetter()) }
}

suspend fun PipelineContext<Unit, ApplicationCall>.registerHandler(unused: Unit) {
    val requestBody = call.receive<RegisterRequest>()

    if (!checkUsername(requestBody.username!!)){
        call.respond(RegisterResponse(status = "failed", message = "用户名只能包含数字和字母。"))
        return
    }

    if (requestBody.password.isNullOrBlank() || (requestBody.password?.length ?: 0) < 6) {
        call.respond(RegisterResponse(status = "failed", message = "密码长度太短。"))
        return
    }

    if (db.users.firstOrNull {
            (it.username eq requestBody.username!!) and (it.password eq requestBody.password!!)
        } != null){
        call.respond(RegisterResponse(status = "failed", message = "该用户名已存在，请更换用户名。"))
        return
    }

    val user = User().apply {
        id = UUID.randomUUID().toString()
        username = requestBody.username!!
        password = requestBody.password!!
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }
    db.users.add(user)

    call.respond(RegisterResponse(status = "succeed", message = null))
}

suspend fun PipelineContext<Unit, ApplicationCall>.loginHandler(unused: Unit){
    val req = call.receive<LoginRequest>()

    if (!checkUsername(req.username!!)){
        call.respond(RegisterResponse(status = "failed", message = "用户名只能包含数字和字母。"))
        return
    }

    val user = db.users.firstOrNull {
        (it.username eq req.username!!) and (it.password eq req.password!!)
    }
    if (user == null){
        call.respond(LoginResponse(status = "failed", message = "用户名或密码错误。", token = null))
        return
    }

    val token = UUID.randomUUID().toString()
    TokenStore.userMap[token] = user.id
    call.respond(LoginResponse(status = "succeed", message = null, token = token))
}

suspend fun PipelineContext<Unit, ApplicationCall>.getUserHandler(unused: Unit) {
    val id = call.parameters["id"] ?: throw InvalidBodyException("?")
    val result = db.users.firstOrNull { it.id eq id } ?: throw Exception("找不到")
    call.respond(result)
}
