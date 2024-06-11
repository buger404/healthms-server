package party.para.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import java.time.LocalDateTime

interface User : Entity<User> {
    companion object : Entity.Factory<User>()
    var id: String
    var username: String
    var password: String
    var createdAt: LocalDateTime
    var updatedAt: LocalDateTime
}

object Users : Table<User>("users") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val username = varchar("username").bindTo { it.username }
    val password = varchar("password").bindTo { it.password }
    val createdAt = datetime("created_at").bindTo { it.createdAt }
    val updatedAt = datetime("updated_at").bindTo { it.updatedAt }
}

val Database.users get() = this.sequenceOf(Users)