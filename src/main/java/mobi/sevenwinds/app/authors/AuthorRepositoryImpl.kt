package mobi.sevenwinds.app.authors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class AuthorRepositoryImpl : AuthorRepository {
    override suspend fun addAuthor(dto: AuthorCreateRequestDto): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val resultRow = AuthorTable
                .insert {
                    it[fullName] = dto.fullName
                }
                .resultedValues?.single() ?: error("Insert failed")

            return@transaction AuthorRecord(
                id = resultRow[AuthorTable.id].value,
                fullName = resultRow[AuthorTable.fullName],
                createdAt = resultRow[AuthorTable.createdAt]
            )
        }
    }

    override suspend fun findAuthorById(id: Int): AuthorRecord? = withContext(Dispatchers.IO) {
        transaction {
            AuthorEntity.findById(id)?.toAuthorRecord()
        }
    }
}