package mobi.sevenwinds.app.authors

import io.restassured.RestAssured
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorApiKtTest : ServerTest() {
    @BeforeEach
    internal fun setUp() {
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun createAuthor() {
        val request = AuthorCreateRequestDto(fullName = "Ivanov Vanya Ivanovich")

        val author = addAuthorRecord(request)

        Assert.assertNotNull(author.id)
        Assert.assertEquals(request.fullName, author.fullName)
        Assert.assertNotNull(author.createdAt)
    }

    private fun addAuthorRecord(authorDto: AuthorCreateRequestDto): AuthorRecord {
        return RestAssured.given()
            .jsonBody(authorDto)
            .post("/authors/add")
            .toResponse()
    }
}
