package mobi.sevenwinds

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.papsign.ktor.openapigen.route.apiRouting
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import mobi.sevenwinds.app.Config
import mobi.sevenwinds.app.authors.AuthorRepository
import mobi.sevenwinds.app.authors.AuthorRepositoryImpl
import mobi.sevenwinds.app.authors.AuthorService
import mobi.sevenwinds.app.budget.BudgetRepository
import mobi.sevenwinds.app.budget.BudgetRepositoryImpl
import mobi.sevenwinds.app.budget.BudgetService
import mobi.sevenwinds.modules.DatabaseFactory
import mobi.sevenwinds.modules.exception.configureErrorHandling
import mobi.sevenwinds.modules.initSwagger
import mobi.sevenwinds.modules.serviceRouting
import mobi.sevenwinds.modules.swaggerRouting
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    Config.init(environment.config)
    DatabaseFactory.init(environment.config)

    initSwagger()

    val authorRepository: AuthorRepository = AuthorRepositoryImpl()
    val budgetRepository: BudgetRepository = BudgetRepositoryImpl()
    val authorService = AuthorService(authorRepository)
    val budgetService = BudgetService(budgetRepository, authorRepository)

    install(DefaultHeaders)

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            registerModule(JodaModule())
            registerModule(Jdk8Module())
        }
    }

    install(Locations) {
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call ->
            Config.logAllRequests ||
                    call.request.path().startsWith("/")
                    && (call.response.status()?.value ?: 0) >= 500
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header(HttpHeaders.ContentType)
        header(HttpHeaders.AccessControlAllowOrigin)
        allowCredentials = true
        allowNonSimpleContentTypes = true
        allowSameOrigin = true
        anyHost()
    }

    apiRouting {
        swaggerRouting(authorService, budgetService)
    }

    routing {
        serviceRouting()
    }

    configureErrorHandling()
}