package mobi.sevenwinds.app.authors

import com.papsign.ktor.openapigen.annotations.type.string.length.MinLength
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.joda.time.DateTime


fun NormalOpenAPIRoute.authors() {
    route("/authors") {
        route("/add").post<Unit, AuthorRecord, AuthorCreateRequestDto>(info("Добавить автора")) { param, body ->
            respond(AuthorService.addRecord(body))
        }
    }
}

data class AuthorRecord(
    var id: Int? = null,
    @MinLength(2) val fullName: String,
    val createdAt: DateTime? = null
)

data class AuthorCreateRequestDto(
    @MinLength(2) val fullName: String
)

data class AuthorResponseDto(
    @MinLength(2) val fullName: String,
    val createdAt: DateTime? = null
)