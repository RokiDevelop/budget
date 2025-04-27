package mobi.sevenwinds.app.authors

interface AuthorRepository {
    suspend fun addAuthor(dto: AuthorCreateRequestDto): AuthorRecord
    suspend fun findAuthorById(id: Int): AuthorRecord?
}
