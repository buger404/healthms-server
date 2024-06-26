package party.para.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.math.BigDecimal
import java.time.LocalDateTime

interface Reservation : Entity<Reservation> {
    companion object : Entity.Factory<Reservation>()
    var id: String
    var user : String
    var chaperone : String
    var price : BigDecimal
}

object Reservations : Table<Reservation>("reservation") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val user = varchar("user").bindTo { it.user }
    val chaperone = varchar("chaperone").bindTo { it.chaperone }
    val price = decimal("price").bindTo { it.price }
}

val Database.reservations get() = this.sequenceOf(Reservations)