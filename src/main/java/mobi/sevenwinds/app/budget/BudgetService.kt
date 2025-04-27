package mobi.sevenwinds.app.budget

import mobi.sevenwinds.app.authors.AuthorRepository
import mobi.sevenwinds.modules.exception.BadRequestException

class BudgetService(
    private val budgetRepository: BudgetRepository,
    private val authorRepository: AuthorRepository
) {
    suspend fun addRecord(record: BudgetRecord): BudgetRecord {
        if (record.authorId != null) {
            authorRepository.findAuthorById(record.authorId) ?: throw BadRequestException("Author not found")
        }
        return budgetRepository.addRecord(record)
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse {
        return budgetRepository.getYearStats(param)
    }
}
