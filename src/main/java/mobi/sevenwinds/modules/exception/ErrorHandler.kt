package mobi.sevenwinds.modules.exception

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.papsign.ktor.openapigen.annotations.type.common.ConstraintViolation
import com.papsign.ktor.openapigen.exceptions.OpenAPIRequiredFieldException
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import org.slf4j.LoggerFactory

fun Application.configureErrorHandling() {
    install(StatusPages) {
        val log = LoggerFactory.getLogger("InternalError")

        exception<NotFoundException> { cause ->
            call.respond(HttpStatusCode.NotFound, cause.message ?: "")
        }
        exception<OpenAPIRequiredFieldException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "")
        }
        exception<MissingKotlinParameterException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "")
        }
        exception<ConstraintViolation> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "")
        }
        exception<BadRequestException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "")
        }
        exception<IllegalArgumentException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "")
            cause.printStackTrace()
            log.error("", cause)
        }
        exception<InvalidFormatException> { cause ->
            call.respond(
                HttpStatusCode.BadRequest, "Invalid value: ${cause.value}")
        }
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "")
            cause.printStackTrace()
            log.error("", cause)
        }
    }
}