package party.para.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDateTime

interface Chaperone : Entity<Chaperone> {
    companion object : Entity.Factory<Chaperone>()
    var id: Int
    var hospital : Int
    var name: String
    var startHour : Int
    var endHour : Int
    var price : Float
    var phone : String
    var reserved : Int
}

object Chaperones : Table<Chaperone>("chaperone") {
    val id = int("id").primaryKey().bindTo { it.id }
    val hospital = int("hospital").bindTo { it.hospital }
    val name = varchar("name").bindTo { it.name }
    val startHour = int("start_hour").bindTo { it.startHour }
    val endHour = int("end_hour").bindTo { it.endHour }
    val price = float("price").bindTo { it.price }
    val phone = varchar("phone").bindTo { it.phone }
    val reserved = int("reserved").bindTo { it.reserved }
}

val Database.chaperones get() = this.sequenceOf(Chaperones)