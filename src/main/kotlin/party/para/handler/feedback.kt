package party.para.handler

import ch.qos.logback.core.subst.Token
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.ktorm.dsl.*
import org.ktorm.entity.*
import party.para.db.db
import party.para.entity.*
import party.para.model.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import party.para.handler.validateToken
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.getFeedbackListHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val chaperone = call.parameters["chaperone"] ?: ""

    val feedbackList = db.from(Feedbacks)
        .innerJoin(Users, on = Feedbacks.user eq Users.id)
        .innerJoin(PraisedTable, on = (Feedbacks.user eq PraisedTable.user) and (Feedbacks.id eq PraisedTable.id))
        .select(Feedbacks.columns + Users.username + PraisedTable.id)
        .where { Feedbacks.chaperone eq chaperone }
        .map { row ->
            val feedback = Feedbacks.createEntity(row)
            val username = row[Users.username]
            mapOf(
                "id" to feedback.id,
                "comment" to feedback.comment,
                "username" to username,
                "sendTime" to feedback.createdAt,
                "likes" to feedback.likes,
                "praised" to feedback.praise,
                "liked" to (!row[PraisedTable.id].isNullOrEmpty())
            )
        }

    call.respond(feedbackList)
}

suspend fun PipelineContext<Unit, ApplicationCall>.getLikeCommentListHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val operatorID = TokenStore.userMap[token] ?: ""

    call.respond(db.praised.filter { it.user eq operatorID}.toList())
}

suspend fun PipelineContext<Unit, ApplicationCall>.checkoutHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val req = call.receive<CheckoutRequest>()
    val operatorID = TokenStore.userMap[token] ?: ""
    val reservationID = req.reservation

    val reservation = db.reservations.firstOrNull { it.id eq reservationID }

    if (reservation == null){
        call.respond(CheckoutResponse("failed", "指定的订单不存在，无法结单。", null))
        return
    }

    val operatorObj = db.users.first { it.id eq operatorID }
    if (reservation.user != operatorID && reservation.chaperone != operatorObj.partTime){
        call.respond(CheckoutResponse("failed", "您没有该订单的操作权限。", null))
        return
    }

    val feedback = Feedback().apply {
        id = UUID.randomUUID().toString()
        user = operatorID
        chaperone = reservation.chaperone
        comment = req.comment
        praise = req.praise
        createdAt = LocalDateTime.now()
        likes = 0
    }
    db.feedbacks.add(feedback)

    val chaperoneObj = db.chaperones.firstOrNull { it.id eq reservation.chaperone }
    if (chaperoneObj != null && req.praise){
        chaperoneObj.praised++
        chaperoneObj.flushChanges()
    }

    db.reservations.removeIf { it.id eq reservationID }

    call.respond(CheckoutResponse("succeed", "您已结单，感谢您对本次服务的评价。", feedback.id))
}

suspend fun PipelineContext<Unit, ApplicationCall>.deleteFeedbackHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val feedbackID = call.parameters["feedback"] ?: ""
    val operatorID = TokenStore.userMap[token] ?: ""

    val feedbackObj = db.feedbacks.firstOrNull { it.id eq feedbackID }

    if (feedbackObj == null){
        call.respond(CheckoutResponse("failed", "指定的评价不存在。", null))
        return
    }

    if (feedbackObj.user != operatorID){
        call.respond(CheckoutResponse("failed", "你没有权限删除这条评价。", null))
        return
    }

    db.praised.removeIf { it.feedback eq feedbackID }
    db.feedbacks.removeIf { it.id eq feedbackID }

    call.respond(CheckoutResponse("succeed", null, null))
}

suspend fun PipelineContext<Unit, ApplicationCall>.likeCommentHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val feedbackID = call.parameters["feedback"] ?: ""
    val operatorID = TokenStore.userMap[token] ?: ""

    val feedbackObj = db.feedbacks.firstOrNull { it.id eq feedbackID }

    if (feedbackObj == null){
        call.respond(CheckoutResponse("failed", "指定的评价不存在。", null))
        return
    }

    if (db.praised.any { (it.user eq operatorID) and (it.feedback eq feedbackID) }){
        call.respond(CheckoutResponse("failed", "你已经赞过这条评价了。", null))
        return
    }

    val praised = Praised().apply {
        id = UUID.randomUUID().toString()
        user = operatorID
        feedback = feedbackID
    }
    db.praised.add(praised)

    feedbackObj.likes++
    feedbackObj.flushChanges()

    call.respond(CheckoutResponse("succeed", null, null))
}

suspend fun PipelineContext<Unit, ApplicationCall>.unlikeCommentHandler(unused: Unit){
    val token = call.parameters["token"]

    if (!validateToken(call, token)){
        return
    }

    val feedbackID = call.parameters["feedback"] ?: ""
    val operatorID = TokenStore.userMap[token] ?: ""

    val feedbackObj = db.feedbacks.firstOrNull { it.id eq feedbackID }

    if (feedbackObj == null){
        call.respond(CheckoutResponse("failed", "指定的评价不存在。", null))
        return
    }

    if (!db.praised.any { (it.user eq operatorID) and (it.feedback eq feedbackID) }){
        call.respond(CheckoutResponse("failed", "你没有赞过这条评价了。", null))
        return
    }

    db.praised.removeIf { (it.user eq operatorID) and (it.feedback eq feedbackID) }

    feedbackObj.likes--
    feedbackObj.flushChanges()

    call.respond(CheckoutResponse("succeed", null, null))
}