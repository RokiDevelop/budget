package mobi.sevenwinds.app.authors

class AuthorService(
    private val authorRepository: AuthorRepository
) {
    suspend fun addRecord(dto: AuthorCreateRequestDto): AuthorRecord {
        return authorRepository.addAuthor(dto)
    }
}
