package kotlet.jetty

import kotlet.ErrorsHandler
import kotlet.Kotlet
import kotlet.Routing
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import org.eclipse.jetty.ee10.servlet.ServletHolder
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.util.thread.ThreadPool
import org.eclipse.jetty.util.thread.VirtualThreadPool

class JettyBuilder internal constructor() {
    private val httpConfiguration = HttpConfiguration().apply {
        sendServerVersion = false
        sendXPoweredBy = false
        sendDateHeader = false
    }

    var port: Int = 80

    var threadPool: ThreadPool = VirtualThreadPool()

    var errorsHandler: ErrorsHandler? = null

    internal fun build(routing: List<Routing>): Server {
        val servlet = Kotlet.servlet(routing, errorsHandler)

        val servletHandler =
            ServletContextHandler(ServletContextHandler.NO_SECURITY + ServletContextHandler.NO_SESSIONS).apply {
                addServlet(ServletHolder(servlet), "/*")
            }

        val http1 = HttpConnectionFactory(httpConfiguration)
        val http2 = HTTP2CServerConnectionFactory()

        return Server(threadPool).apply {
            handler = GzipHandler(servletHandler)

            // HTTP
            addConnector(
                ServerConnector(server, http1, http2).apply {
                    this.port = this@JettyBuilder.port
                }
            )
        }
    }
}
