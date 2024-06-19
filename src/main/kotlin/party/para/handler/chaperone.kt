package party.para.handler

import ch.qos.logback.core.subst.Token
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import party.para.db.db
import party.para.entity.User
import party.para.entity.chaperones
import party.para.entity.users
import party.para.model.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import party.para.handler.validateToken
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.getChaperoneListHandler(unused: Unit){
    val id = call.parameters["hospital"]?.toInt() ?: -1
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    if (id == -1){
        call.respond(db.chaperones.toList())
    }else{
        call.respond(db.chaperones.filter { it.hospital eq id }.toList())
    }
}