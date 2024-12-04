package swagger.ui

import kotlet.Handler
import kotlet.HttpCall
import kotlet.Routing

/**
 * Install OpenAPI endpoint to the routing
 *
 * @param configure Configuration of the OpenAPI endpoint
 */
fun Routing.installSwaggerUI(configure: SwaggerUIConfigBuilder.() -> Unit) {
    val builder = SwaggerUIConfigBuilder()
    builder.configure()
    val config = builder.build()

    fun serveResource(path: String, contentType: String) {
        get("${config.path}/$path", SwaggerUIResourceHandler("/ui/$path", contentType))
    }

    // Serve swagger ui static files
    get(config.path, RedirectHandler("${config.path}/index.html"))
    serveResource("index.html", MediaTypes.TEXT_HTML)
    serveResource("index.css", MediaTypes.TEXT_CSS)
    serveResource("swagger-ui.css", MediaTypes.TEXT_CSS)
    serveResource("swagger-ui-standalone-preset.js", MediaTypes.JAVASCRIPT)
    serveResource("swagger-ui-bundle.js", MediaTypes.JAVASCRIPT)
    get("${config.path}/swagger-initializer.js", SwaggerUIInitializerHandler(openAPIPath = config.openAPIPath))
}

private class RedirectHandler(private val location: String) : Handler {
    override fun invoke(call: HttpCall) {
        call.rawResponse.sendRedirect(location)
    }
}

private class SwaggerUIResourceHandler(
    resourcePath: String,
    private val contentType: String,
) : Handler {
    private val bytes: ByteArray = loadSwaggerUIResource(resourcePath)

    override fun invoke(call: HttpCall) {
        call.rawResponse.contentType = contentType
        call.respondBytes(bytes)
    }
}

private fun loadSwaggerUIResource(path: String): ByteArray {
    val resourceStream = requireNotNull(SwaggerUIResourceHandler::class.java.getResourceAsStream(path)) {
        "Resource not found: $path"
    }
    return resourceStream.readBytes()
}

private class SwaggerUIInitializerHandler(openAPIPath: String) : Handler {
    private val bytes = """
        window.onload = function() {
          window.ui = SwaggerUIBundle({
            url: "$openAPIPath",
            dom_id: '#swagger-ui',
            deepLinking: true,
            presets: [
              SwaggerUIBundle.presets.apis,
              SwaggerUIStandalonePreset
            ],
            plugins: [
              SwaggerUIBundle.plugins.DownloadUrl
            ],
            layout: "StandaloneLayout"
          });
        };
    """.trimIndent().toByteArray()

    override fun invoke(call: HttpCall) {
        call.rawResponse.contentType = MediaTypes.JAVASCRIPT
        call.respondBytes(bytes)
    }
}

/**
 * Media types for Swagger UI resources
 */
private object MediaTypes {
    const val TEXT_HTML = "text/html"
    const val TEXT_CSS = "text/css"
    const val JAVASCRIPT = "application/javascript"
}
