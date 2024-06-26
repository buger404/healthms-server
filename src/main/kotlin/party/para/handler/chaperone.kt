package party.para.handler

import ch.qos.logback.core.subst.Token
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.less
import org.ktorm.entity.*
import party.para.db.db
import party.para.entity.*
import party.para.model.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import party.para.handler.validateToken
import java.math.BigDecimal
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

suspend fun checkChaperoneData(data : JoinChaperoneRequest, call : ApplicationCall) : Boolean{
    if (!db.hospitals.any { it.id eq data.hospital }){
        call.respond(JoinChaperoneResponse("failed", "指定的医院不存在。", null))
        return false
    }

    if (data.price < BigDecimal.ZERO){
        call.respond(JoinChaperoneResponse("failed", "设定的价格不能是负数。", null))
        return false
    }

    if (data.startHour >= data.endHour){
        call.respond(JoinChaperoneResponse("failed", "兼职开始时间必须早于结束时间。", null))
        return false
    }

    return true
}

suspend fun PipelineContext<Unit, ApplicationCall>.joinChaperoneHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val req = call.receive<JoinChaperoneRequest>()

    val userID = TokenStore.userMap[token] ?: ""
    val userObj = db.users.first { it.id eq userID }

    if (userObj.partTime.isNotEmpty()){
        call.respond(JoinChaperoneResponse("failed", "在发起一个新的兼职之前，必须结束当前的兼职。", null))
        return
    }

    if (!checkChaperoneData(req, call)){
        return
    }

    val chaperone = Chaperone().apply {
        id = UUID.randomUUID().toString()
        hospital = req.hospital
        name = req.name
        startHour = req.startHour
        endHour = req.endHour
        price = req.price
        phone = req.phone
        reserved = 0
        praised = 0
    }
    db.chaperones.add(chaperone)

    userObj.partTime = chaperone.id

    call.respond(JoinChaperoneResponse("succeed", "您已加入陪诊师兼职！", chaperone.id))
}

suspend fun PipelineContext<Unit, ApplicationCall>.quitChaperoneHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val userID = TokenStore.userMap[token] ?: ""
    val userObj = db.users.first { it.id eq userID }

    if (userObj.partTime.isEmpty()){
        call.respond(JoinChaperoneResponse("failed", "你当前没有兼职陪诊师，无需退出兼职。", null))
        return
    }

    if (!db.chaperones.any { it.id eq userObj.partTime }){
        userObj.partTime = ""
        call.respond(JoinChaperoneResponse("failed", "你当前没有兼职陪诊师信息不存在，已重置为未兼职状态。", null))
        return
    }

    for ( reservation in db.reservations.filter { it.chaperone eq userObj.partTime }){
        try{
            cancelReservation(userID, reservation.id)
        }catch (ex : Exception){
            call.respond(JoinChaperoneResponse("failed", "退出兼职失败！无法取消现有预约，因为：" + ex.message, null))
            return
        }
    }

    db.chaperones.removeIf { it.id eq userObj.partTime }
    userObj.partTime = ""

    call.respond(JoinChaperoneResponse("succeed", "您已退出陪诊师兼职。", null))
}

suspend fun PipelineContext<Unit, ApplicationCall>.updateChaperoneHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val req = call.receive<JoinChaperoneRequest>()

    val userID = TokenStore.userMap[token] ?: ""
    val userObj = db.users.first { it.id eq userID }

    if (userObj.partTime.isEmpty()){
        call.respond(JoinChaperoneResponse("failed", "你当前没有兼职陪诊师，兼职信息更新失败。", null))
        return
    }

    if (!checkChaperoneData(req, call)){
        return
    }

    val chaperone = db.chaperones.firstOrNull { it.id eq userObj.partTime }
    if (chaperone == null){
        userObj.partTime = ""
        call.respond(JoinChaperoneResponse("failed", "你当前没有兼职陪诊师信息不存在，已重置为未兼职状态。", null))
        return
    }

    chaperone.apply {
        hospital = req.hospital
        name = req.name
        startHour = req.startHour
        endHour = req.endHour
        price = req.price
        phone = req.phone
    }

    call.respond(JoinChaperoneResponse("succeed", "兼职信息修改成功！", null))
}