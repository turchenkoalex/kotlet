package kotlet.openapi.dsl

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import kotlin.reflect.KClass

/**
 * Adds an API response to the ApiResponses object.
 *
 * @param code The HTTP status code for the response.
 * @param description A brief description of the response.
 * @param contentType The content type of the response.
 * @param configure An optional configuration block for the ApiResponse.
 */
fun Operation.response(
    code: Int,
    description: String,
    contentType: ContentType = ContentTypes.NoContent,
    configure: ApiResponse.() -> Unit = {}
) {
    if (responses == null) {
        responses = ApiResponses()
    }

    val apiResponse = ApiResponse()
    apiResponse.description(description)
    apiResponse.addMediaType(contentType)
    apiResponse.configure()

    responses.addApiResponse(code.toString(), apiResponse)
}

/**
 * Adds a JSON API response to the ApiResponses object.
 *
 * @param code The HTTP status code for the response.
 * @param description A brief description of the response.
 * @param clazz The KClass representing the type of the response body.
 */
fun Operation.jsonResponse(
    code: Int,
    description: String,
    clazz: KClass<*>
) {
    return response(code, description, ContentTypes.ApplicationJson(clazz))
}

/**
 * Adds a JSON API response to the ApiResponses object.
 *
 * @param code The HTTP status code for the response.
 * @param description A brief description of the response.
 */
inline fun <reified T> Operation.jsonResponse(
    code: Int,
    description: String
) {
    return jsonResponse(code, description, T::class)
}

/**
 * Adds a 404 Not found response to the ApiResponses object.
 *
 * @param description A brief description of the response.
 * @param configure An optional configuration block for the ApiResponse.
 */
fun Operation.notFound(description: String, configure: ApiResponse.() -> Unit = {}) {
    return response(404, description, configure = configure)
}

/**
 * Adds a 400 Bad request response to the ApiResponses object.
 *
 * @param description A brief description of the response.
 * @param configure An optional configuration block for the ApiResponse.
 */
fun Operation.badRequest(description: String, configure: ApiResponse.() -> Unit = {}) {
    return response(400, description, configure = configure)
}
