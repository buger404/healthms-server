package para.party.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    var id: String
    var username: String
    var password: String
    var createdAt: Long
    var updatedAt: Long
}

object Users : Table<User>("users") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val username = varchar("username").bindTo { it.username }
    val password = varchar("password").bindTo { it.password }
    val createdAt = long("created_at").bindTo { it.createdAt }
    val updatedAt = long("updated_at").bindTo { it.updatedAt }
}

val Database.users get() = this.sequenceOf(Users)
