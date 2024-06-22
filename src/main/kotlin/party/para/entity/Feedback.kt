package party.para.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import party.para.entity.Users.bindTo
import java.time.LocalDateTime

interface Feedback : Entity<Feedback> {
    companion object : Entity.Factory<Feedback>()
    var id: String
    var user : String
    var chaperone : String
    var praise : Boolean
    var comment : String
    var createdAt: LocalDateTime
    var likes : Int
}

object Feedbacks : Table<Feedback>("feedback") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val user = varchar("user").bindTo { it.user }
    val chaperone = varchar("chaperone").bindTo { it.chaperone }
    val praise = boolean("praise").bindTo { it.praise }
    val comment = varchar("comment").bindTo { it.comment }
    val createdAt = datetime("created_at").bindTo { it.createdAt }
    val likes = int("likes").bindTo { it.likes }
}

val Database.feedbacks get() = this.sequenceOf(Feedbacks)