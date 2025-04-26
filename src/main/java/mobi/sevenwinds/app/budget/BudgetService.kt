package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.authors.AuthorEntity
import mobi.sevenwinds.app.authors.AuthorTable
import mobi.sevenwinds.modules.exception.BadRequestException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                body.authorId?.let { authorId ->
                    author = AuthorEntity.findById(authorId) ?: throw BadRequestException("Author with id=$authorId not found")
                }
            }

            entity.refresh(true)

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val baseCondition = BudgetTable.year.eq(param.year)

            val authorCondition = param.author
                ?.takeIf { it.isNotBlank() }
                ?.let { AuthorTable.fullName.lowerCase().like("%${it.toLowerCase()}%") }
                ?: Op.TRUE

            val joined = BudgetTable.leftJoin(
                otherTable = AuthorTable,
                onColumn = { BudgetTable.author },
                otherColumn = { AuthorTable.id }
            )

            val query = joined
                .slice(BudgetTable.columns)
                .select {
                    baseCondition and authorCondition
                }
                .orderBy(
                    BudgetTable.month to SortOrder.ASC,
                    BudgetTable.amount to SortOrder.DESC
                )
                .limit(param.limit, param.offset)


            val aggRows = BudgetTable
                .slice(BudgetTable.year.count(), BudgetTable.type, BudgetTable.amount.sum())
                .select { BudgetTable.year eq param.year }
                .groupBy(BudgetTable.type)
                .toList()

            val total = aggRows.sumOf { row -> row[BudgetTable.year.count()] }

            val data = BudgetEntity.wrapRows(query).map { it.toResponseDto() }

            val sumByType = aggRows.associate { row ->
                val type = row[BudgetTable.type].toString()
                val sum = row[BudgetTable.amount.sum()] ?: 0
                type to sum
            }
            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}