package party.para.db

import org.ktorm.database.Database
import org.ktorm.database.SqlDialect
import org.ktorm.expression.QueryExpression
import org.ktorm.expression.SqlFormatter

class SinodbDialect : SqlDialect {
    override fun createSqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int): SqlFormatter {
        return SinodbFormatter(database, beautifySql, indentSize)
    }
}

class SinodbFormatter(database: Database, beautifySql: Boolean, indentSize: Int) :
    SqlFormatter(database, beautifySql, indentSize) {

    override fun writePagination(expr: QueryExpression) {
        newLine(Indentation.SAME)

        // sinodbms 的 FIRST 和 SKIP 子句不能用参数绑定
        if (expr.limit != null) {
            writeKeyword("FIRST ${expr.limit} ")
        }
        if (expr.offset != null) {
            writeKeyword("SKIP ${expr.offset} ")
        }
    }
}
