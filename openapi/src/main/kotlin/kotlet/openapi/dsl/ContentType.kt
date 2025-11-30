package kotlet.openapi.dsl

import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import kotlet.openapi.generateSchema
import kotlin.reflect.KClass

/**
 * Represents a content type in OpenAPI specifications.
 */
interface ContentType {
    /**
     * The media type string (e.g., "application/json").
     */
    val mediaTypeName: String

    /**
     * Generates the OpenAPI Content object for this content type.
     */
    fun mediaType(): MediaType = MediaType()
}

/**
 * Common content types used in OpenAPI specifications.
 */
sealed class ContentTypes(override val mediaTypeName: String) : ContentType {
    /**
     * No content type
     */
    data object NoContent : ContentTypes("")

    /**
     * JSON content type
     */
    data class ApplicationJson(val clazz: KClass<*>) : ContentTypes("application/json") {
        override fun mediaType(): MediaType {
            return MediaType().apply {
                schema = generateSchema(clazz)
            }
        }
    }

    /**
     * XML content type
     */
    data class ApplicationXml(val clazz: KClass<*>) : ContentTypes("application/xml") {
        override fun mediaType(): MediaType {
            return MediaType().apply {
                schema = generateSchema(clazz)
            }
        }
    }

    /**
     * Plain text content type
     */
    data object TextPlain : ContentTypes("text/plain")

    /**
     * HTML content type
     */
    data object TextHtml : ContentTypes("text/html")

    /**
     * Binary stream content type
     */
    data object ApplicationOctetStream : ContentTypes("application/octet-stream")
}

/**
 * Adds a media type to the Content object of the ApiResponse.
 *
 * @param contentType The content type to add.
 */
internal fun ApiResponse.addMediaType(contentType: ContentType) {
    if (contentType.mediaTypeName.isNotEmpty()) {
        if (content == null) {
            content = Content()
        }
        content.addMediaType(contentType)
    }
}

/**
 * Adds a media type to the Content object of the RequestBody.
 *
 * @param contentType The content type to add.
 */
internal fun RequestBody.addContentType(contentType: ContentType) {
    if (contentType.mediaTypeName.isNotEmpty()) {
        if (content == null) {
            content = Content()
        }
        content.addMediaType(contentType)
    }
}

private fun Content.addMediaType(contentType: ContentType) {
    if (contentType.mediaTypeName.isNotEmpty()) {
        addMediaType(contentType.mediaTypeName, contentType.mediaType())
    }
}
