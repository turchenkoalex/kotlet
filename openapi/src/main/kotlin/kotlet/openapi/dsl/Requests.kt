package kotlet.openapi.dsl

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.parameters.RequestBody
import kotlin.reflect.KClass

/**
 * Adds a request body to the Operation.
 *
 * @param description A brief description of the request body.
 * @param contentType The content type of the request body.
 * @param configure An optional configuration block for the RequestBody.
 */
fun Operation.request(
    description: String,
    contentType: ContentType = ContentTypes.NoContent,
    configure: RequestBody.() -> Unit = {}
) {
    requestBody = RequestBody()
    requestBody.description = description
    requestBody.addContentType(contentType)
    requestBody.configure()
}

/**
 * Adds a JSON request body to the Operation.
 *
 * @param description A brief description of the request body.
 * @param clazz The KClass representing the type of the request body.
 */
fun Operation.jsonRequest(description: String, clazz: KClass<*>) {
    return request(description, ContentTypes.ApplicationJson(clazz))
}

/**
 * Adds a JSON request body to the Operation.
 *
 * @param description A brief description of the request body.
 */
inline fun <reified T: Any> Operation.jsonRequest(description: String) = jsonRequest(description, T::class)
