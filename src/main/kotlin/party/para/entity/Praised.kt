package party.para.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import party.para.entity.Users.bindTo
import java.time.LocalDateTime

interface Praised : Entity<Praised> {
    companion object : Entity.Factory<Praised>()
    var id: String
    var user : String
    var feedback : String
}

object PraisedTable : Table<Praised>("praised") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val user = varchar("user").bindTo { it.user }
    val feedback = varchar("feedback").bindTo { it.feedback }
}

val Database.praised get() = this.sequenceOf(PraisedTable)