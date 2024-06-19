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
import org.ktorm.entity.all
import org.ktorm.entity.firstOrNull
import party.para.db.db
import party.para.entity.User
import party.para.entity.chaperones
import party.para.entity.hospitals
import party.para.entity.users
import party.para.model.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import party.para.handler.validateToken
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.getHospitalListHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    call.respond(db.hospitals)
}