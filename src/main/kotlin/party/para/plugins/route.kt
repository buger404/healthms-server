package party.para.plugins

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.slf4j.LoggerFactory
import party.para.handler.*
import java.util.*

fun Application.registerRoute() {
    val logger = LoggerFactory.getLogger(this::class.java)

    install(AutoHeadResponse)
    routing {
        get("/") {
            call.respondText("Health MS Server!")
        }

        post("/register", PipelineContext<Unit, ApplicationCall>::registerHandler)
        post("/login", PipelineContext<Unit, ApplicationCall>::loginHandler)
        post("/logout", PipelineContext<Unit, ApplicationCall>::logoutHandler)

        get("/users/info", PipelineContext<Unit, ApplicationCall>::getUserHandler)
        post("/users/recharge", PipelineContext<Unit, ApplicationCall>::rechargeUserHandler)

        get("/chaperone/list", PipelineContext<Unit, ApplicationCall>::getChaperoneListHandler)
        post("/chaperone/join", PipelineContext<Unit, ApplicationCall>::joinChaperoneHandler)
        post("/chaperone/update", PipelineContext<Unit, ApplicationCall>::updateChaperoneHandler)
        post("/chaperone/quit", PipelineContext<Unit, ApplicationCall>::quitChaperoneHandler)

        get("/hospital/list", PipelineContext<Unit, ApplicationCall>::getHospitalListHandler)

        get("/reservation/list", PipelineContext<Unit, ApplicationCall>::getReservationListHandler)
        post("/reservation/submit", PipelineContext<Unit, ApplicationCall>::submitReservationHandler)
        post("/reservation/cancel", PipelineContext<Unit, ApplicationCall>::cancelReservationHandler)
        post("/reservation/checkout", PipelineContext<Unit, ApplicationCall>::checkoutHandler)

        get("/feedback/list", PipelineContext<Unit, ApplicationCall>::getFeedbackListHandler)
        get("/feedback/like/list", PipelineContext<Unit, ApplicationCall>::getLikeCommentListHandler)
        post("/feedback/delete", PipelineContext<Unit, ApplicationCall>::deleteFeedbackHandler)
        post("/feedback/like", PipelineContext<Unit, ApplicationCall>::likeCommentHandler)
        post("/feedback/unlike", PipelineContext<Unit, ApplicationCall>::unlikeCommentHandler)
    }
}
