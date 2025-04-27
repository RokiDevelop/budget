package mobi.sevenwinds.app.budget

interface BudgetRepository {
    suspend fun addRecord(record: BudgetRecord): BudgetRecord
    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse
}
