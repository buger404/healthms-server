package party.para.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDateTime

interface Hospital : Entity<Hospital> {
    companion object : Entity.Factory<Hospital>()
    var id: Int
    var name: String
    var location : String
}

object Hospitals : Table<Hospital>("hospital") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val location = varchar("location").bindTo { it.location }
}

val Database.hospitals get() = this.sequenceOf(Hospitals)