package pt.isel.mapper

import java.sql.ResultSet
import java.sql.SQLException
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext

class InstantMapper : ColumnMapper<Instant> {
    @Throws(SQLException::class)
    override fun map(
        rs: ResultSet,
        columnNumber: Int,
        ctx: StatementContext,
    ): Instant = Instant.fromEpochSeconds(rs.getLong(columnNumber))
}
