package party.para.handler

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.ktorm.dsl.*
import org.ktorm.entity.*
import party.para.db.db
import party.para.entity.*
import party.para.model.*
import java.time.LocalDateTime
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.getReservationListHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val user = TokenStore.userMap[token] ?: ""
    val list = db.from(Reservations)
        .innerJoin(Chaperones, on = Chaperones.id eq Reservations.chaperone)
        .select(Reservations.columns + Chaperones.columns)
        .where(Reservations.user eq user)
        .map { row ->
            val reservation = Reservations.createEntity(row)
            val chaperone = Chaperones.createEntity(row)
            mapOf(
                "id" to reservation.id,
                "chaperone" to reservation.chaperone,
                "price" to reservation.price,
                "time" to reservation.time,
                "chaperoneInfo" to chaperone
            )
        }

    call.respond(list)
}

suspend fun PipelineContext<Unit, ApplicationCall>.getChaperoneReservationListHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val user = TokenStore.userMap[token] ?: ""
    val list = db.from(Reservations)
        .innerJoin(Users, on = Users.id eq Reservations.user)
        .select(Reservations.columns + Users.username)
        .where(Reservations.chaperone eq db.users.first { it.id eq user }.partTime)
        .map { row ->
            val reservation = Reservations.createEntity(row)
            mapOf(
                "id" to reservation.id,
                "chaperone" to reservation.chaperone,
                "price" to reservation.price,
                "time" to reservation.time,
                "username" to row[Users.username]
            )
        }

    call.respond(list)
}

suspend fun PipelineContext<Unit, ApplicationCall>.isReservedHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val user = TokenStore.userMap[token] ?: ""
    val chaperone = call.parameters["chaperone"] ?: ""

    call.respond(
        mapOf(
            "status" to "succeed",
            "id" to (db.reservations.firstOrNull { (it.chaperone eq chaperone) and (it.user eq user) }?.id ?: "")
        )
    )
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

    if (chaperoneObj.id == userObj.partTime){
        call.respond(SubmitReservationResponse("failed", "您不能预约您自己。"))
        return
    }

    if (userObj.money < chaperoneObj.price){
        call.respond(SubmitReservationResponse("failed", "余额不足。"))
        return
    }

    val reservation = Reservation().apply {
        id = UUID.randomUUID().toString()
        user = userID
        chaperone = chaperoneID
        price = chaperoneObj.price
        time = LocalDateTime.parse(call.parameters["date"])
    }
    db.reservations.add(reservation)

    userObj.money -= chaperoneObj.price
    userObj.flushChanges()

    chaperoneObj.reserved++
    chaperoneObj.flushChanges()

    val chaperoneUser = db.users.firstOrNull { it.partTime eq chaperoneID }
    if (chaperoneUser != null){
        chaperoneUser.money += chaperoneObj.price
        chaperoneUser.flushChanges()
    }

    call.respond(SubmitReservationResponse(status = "succeed", message = reservation.id))
}

fun cancelReservation(operatorID : String, reservationID : String){
    val reservation = db.reservations.firstOrNull { it.id eq reservationID }

    if (reservation == null){
        throw Exception("指定的预约不存在！")
    }

    val operatorObj = db.users.first { it.id eq operatorID }
    if (reservation.user != operatorID && reservation.chaperone != operatorObj.partTime){
        throw Exception("您无权取消此预约。")
    }

    val userObj = db.users.first { it.id eq reservation.user }
    userObj.money += reservation.price
    userObj.flushChanges()

    val chaperoneUser = db.users.firstOrNull { it.partTime eq reservation.chaperone }
    if (chaperoneUser != null){
        chaperoneUser.money -= reservation.price
        chaperoneUser.flushChanges()
    }
    val chaperoneObj = db.chaperones.firstOrNull { it.id eq reservation.chaperone }
    if (chaperoneObj != null){
        chaperoneObj.reserved--
        chaperoneObj.flushChanges()
    }

    db.reservations.removeIf { it.id eq reservationID }
}

suspend fun PipelineContext<Unit, ApplicationCall>.cancelReservationHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val operatorID = TokenStore.userMap[token] ?: ""
    val reservationID = call.parameters["reservation"] ?: ""

    try{
        cancelReservation(operatorID, reservationID)
    }catch (ex : Exception){
        call.respond(SubmitReservationResponse("failed", ex.message))
    }

    call.respond(SubmitReservationResponse(status = "succeed", message = null))
}