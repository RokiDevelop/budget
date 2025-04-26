package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .select { BudgetTable.year eq param.year }
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

            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }

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