package party.para.handler

import ch.qos.logback.core.subst.Token
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.first
import org.ktorm.entity.firstOrNull
import party.para.db.db
import party.para.entity.User
import party.para.entity.users
import party.para.model.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

object TokenStore {
    val userMap: MutableMap<String, String> = mutableMapOf()
}

fun checkUsername(username : String) : Boolean{
    return !username.any { !(it.isDigit() || it.isLetter()) }
}

suspend fun validateToken(call : ApplicationCall, token : String?) : Boolean{
    if (!TokenStore.userMap.containsKey(token)){
        call.respond(HttpStatusCode.Forbidden, ErrorResponse("请先登录！"));
        return false;
    }
    return true;
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

    if (db.users.firstOrNull { it.username eq requestBody.username!! } != null){
        call.respond(RegisterResponse(status = "failed", message = "该用户名已存在，请更换用户名。"))
        return
    }

    val user = User().apply {
        id = UUID.randomUUID().toString()
        username = requestBody.username!!
        password = requestBody.password!!
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
        money = BigDecimal.ZERO
        partTime = ""
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

    val user = db.users.firstOrNull { (it.username eq req.username!!) }
    if (user == null){
        call.respond(LoginResponse(status = "failed", message = "用户不存在。", token = null))
        return
    }

    if (user.password != req.password){
        call.respond(LoginResponse(status = "failed", message = "用户名或密码错误。", token = null))
        return
    }

    val token = UUID.randomUUID().toString()
    TokenStore.userMap[token] = user.id
    call.respond(LoginResponse(status = "succeed", message = null, token = token))
}

suspend fun PipelineContext<Unit, ApplicationCall>.getUserHandler(unused: Unit) {
    val token = call.parameters["token"]
    if (!validateToken(call, token)){
        return
    }

    val result = db.users.firstOrNull { it.id eq (TokenStore.userMap[token] ?: "") }

    if (result != null) {
        call.respond(result)
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.rechargeUserHandler(unused: Unit) {
    val token = call.parameters["token"]
    if (!validateToken(call, token)){
        return
    }

    val money = call.parameters["money"]?.toBigDecimal() ?: BigDecimal.ZERO
    val user = db.users.first { it.id eq (TokenStore.userMap[token] ?: "") }

    user.money += money

    call.respond(user)
}