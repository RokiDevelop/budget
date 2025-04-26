package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.app.authors.AuthorCreateRequestDto
import mobi.sevenwinds.app.authors.AuthorRecord
import mobi.sevenwinds.app.authors.AuthorTable
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BudgetApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { BudgetTable.deleteAll() }
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun testFilterByAuthorName() {
        val authorName1 = "Petrov Petya Petrovich"
        val authorName2 = "Sidorov Sidor Sidorovich"
        val authorName3 = "Stepanov Petya Stepanovich"

        val author1 = addAuthorRecord(authorName1)
        val author2 = addAuthorRecord(authorName2)
        val author3 = addAuthorRecord(authorName3)

        addRecord(BudgetRecord(2024, 9, 300, BudgetType.Приход, author3.id))
        addRecord(BudgetRecord(2024, 6, 150, BudgetType.Расход, author1.id))
        addRecord(BudgetRecord(2024, 5, 100, BudgetType.Приход, author1.id))
        addRecord(BudgetRecord(2024, 8, 250, BudgetType.Приход, author2.id))
        addRecord(BudgetRecord(2024, 10, 350, BudgetType.Расход, null))
        addRecord(BudgetRecord(2024, 7, 200, BudgetType.Приход, author2.id))

        val response = RestAssured.given()
            .queryParam("limit", 10)
            .queryParam("offset", 0)
            .queryParam("author", "Petya")
            .get("/budget/year/2024/stats")
            .toResponse<BudgetYearStatsResponse>()

        Assert.assertEquals(3, response.items.size)

        val fullNames = response.items.mapNotNull { it.author?.fullName }.toSet()

        Assert.assertTrue(fullNames.contains(authorName1))
        Assert.assertTrue(fullNames.contains(authorName3))

        val months = response.items.map { it.month }

        Assert.assertTrue(months == months.sorted())
    }

    @Test
    fun testBudgetPagination() {
        addRecord(BudgetRecord(2020, 5, 10, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 5, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 20, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 30, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 40, BudgetType.Приход))
        addRecord(BudgetRecord(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(5, response.total)
                Assert.assertEquals(3, response.items.size)
                Assert.assertEquals(105, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addRecord(BudgetRecord(2020, 5, 100, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 1, 5, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 1, 30, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 400, BudgetType.Приход))

        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)

                Assert.assertEquals(30, response.items[0].amount)
                Assert.assertEquals(5, response.items[1].amount)
                Assert.assertEquals(400, response.items[2].amount)
                Assert.assertEquals(100, response.items[3].amount)
                Assert.assertEquals(50, response.items[4].amount)
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetRecord(2020, -5, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRecord(2020, 15, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)
    }

    private fun addRecord(record: BudgetRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecord>().let { response ->
                Assert.assertEquals(record, response)
            }
    }

    private fun addAuthorRecord(fullName: String): AuthorRecord {
        return RestAssured.given()
            .jsonBody(AuthorCreateRequestDto(fullName))
            .post("/authors/add")
            .toResponse()
    }
}