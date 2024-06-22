package party.para.handler

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import party.para.db.db
import party.para.entity.*
import party.para.model.*
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.getReservationListHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val user = TokenStore.userMap[token] ?: ""
    call.respond(db.reservations.filter { it.user eq user }.toList())
}

suspend fun PipelineContext<Unit, ApplicationCall>.submitReservationHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val userID = TokenStore.userMap[token] ?: ""
    val chaperoneID = call.parameters["chaperone"] ?: ""

    val chaperoneObj = db.chaperones.firstOrNull { it.id eq chaperoneID }

    if (chaperoneObj == null){
        call.respond(SubmitReservationResponse("failed", "指定的陪诊师不存在！"))
        return
    }

    if (db.reservations.any { (it.user eq userID) and (it.chaperone eq chaperoneID) }){
        call.respond(SubmitReservationResponse("failed", "不能重复预约同一个陪诊师。"))
        return
    }

    val userObj = db.users.first { it.id eq userID }
    if (userObj.money < chaperoneObj.price){
        call.respond(SubmitReservationResponse("failed", "余额不足。"))
        return
    }

    val reservation = Reservation().apply {
        id = UUID.randomUUID().toString()
        user = userID
        chaperone = chaperoneID
    }
    db.reservations.add(reservation)

    userObj.money -= chaperoneObj.price
    chaperoneObj.reserved++

    call.respond(SubmitReservationResponse(status = "succeed", message = null))
}

suspend fun PipelineContext<Unit, ApplicationCall>.cancelReservationHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val userID = TokenStore.userMap[token] ?: ""
    val chaperoneID = call.parameters["chaperone"] ?: ""

    val chaperoneObj = db.chaperones.firstOrNull { it.id eq chaperoneID }

    if (chaperoneObj == null){
        call.respond(SubmitReservationResponse("failed", "指定的陪诊师不存在！"))
        return
    }

    if (!db.reservations.any { (it.user eq userID) and (it.chaperone eq chaperoneID) }){
        call.respond(SubmitReservationResponse("failed", "指定的预约不存在！"))
        return
    }

    db.reservations.removeIf { (it.user eq userID) and (it.chaperone eq chaperoneID) }

    val userObj = db.users.first { it.id eq userID }
    userObj.money += chaperoneObj.price
    chaperoneObj.reserved--

    call.respond(SubmitReservationResponse(status = "succeed", message = null))
}