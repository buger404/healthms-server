package party.para.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDateTime

interface Reservation : Entity<Reservation> {
    companion object : Entity.Factory<Reservation>()
    var id: String
    var user : String
    var chaperone : Int
}

object Reservations : Table<Reservation>("reservation") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val user = varchar("user").bindTo { it.user }
    val chaperone = int("chaperone").bindTo { it.chaperone }
}

val Database.reservations get() = this.sequenceOf(Reservations)