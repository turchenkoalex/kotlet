package jetty

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlet.ErrorsHandler
import kotlet.Routing
import kotlet.jetty.jetty
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

fun startJettyServer(
    routing: Routing,
    onStart: () -> Unit = {},
    onShutdown: () -> Unit = {}
) {
    val server = jetty(routing) {
        port = 8080
        errorsHandler = CustomErrorsHandler
    }

    server.start()

    println("Open http://localhost:8080/ in your browser")

    onStart()

    awaitShutdown(onShutdown)
}

private fun awaitShutdown(onShutdown: () -> Unit) {
    val latch = CountDownLatch(1)
    val hook = thread(start = false) {
        onShutdown()
        latch.countDown()
    }

    Runtime.getRuntime().addShutdownHook(hook)
    latch.await()
}

/**
 * Custom error handler that logs the exception and returns a 500 status code.
 */
private object CustomErrorsHandler : ErrorsHandler {
    override fun routeNotFound(request: HttpServletRequest, response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_NOT_FOUND
        response.writer.write("Not found")
    }

    override fun internalServerError(request: HttpServletRequest, response: HttpServletResponse, e: Exception) {
        response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        e.printStackTrace()
    }
}
