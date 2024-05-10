package party.para.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.ktorm.database.Database

val dataSource by lazy {
    HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:sinodbms-sqli://localhost:18411/healthms:SINODBMSSERVER=sinodb_demo"
        username = "sinodbms"
        password = "sinodbms"
        driverClassName = com.sinodbms.jdbc.IfxDriver::class.java.name
        maximumPoolSize = 10
        isAutoCommit = true
    })
}

val db by lazy {
    Database.connect(dataSource, dialect = SinodbDialect())
}
