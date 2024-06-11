package party.para.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.ktorm.database.Database

val dataSource by lazy {
    HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:sinodbms-sqli://localhost:9563/healthms:SINODBMSSERVER=sinodb_demo;OPTOFC=1;IFX_DEFERRED_PREPARE=1;IFX_LOCK_MODE_WAIT=30;DB_LOCALE=en_US.8859-1;CLIENT_LOCALE=en_US.utf8;NEWCODESET=utf8,8859-1,819;charset=en_US.utf8;optcompind=0"
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
