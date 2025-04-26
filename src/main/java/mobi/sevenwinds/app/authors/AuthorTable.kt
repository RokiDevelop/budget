package mobi.sevenwinds.app.authors

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.CurrentDateTime


object AuthorTable : IntIdTable("author") {
    val fullName = varchar("full_name", 100)
    val createdAt = datetime("created_at")
        .defaultExpression(CurrentDateTime())
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fullName by AuthorTable.fullName
    var createdAt by AuthorTable.createdAt

    fun toAuthorRecord(): AuthorRecord {
        return AuthorRecord(
            id = id.value,
            fullName = fullName,
            createdAt = createdAt
        )
    }

    fun toAuthorResponseDto(): AuthorResponseDto {
        return AuthorResponseDto(
            fullName = fullName,
            createdAt = createdAt
        )
    }
}