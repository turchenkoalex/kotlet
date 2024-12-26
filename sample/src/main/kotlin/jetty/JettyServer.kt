package jetty

import jakarta.servlet.http.HttpServlet
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import org.eclipse.jetty.ee10.servlet.ServletHolder
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.util.thread.ThreadPool
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class JettyServer(
    port: Int,
    routes: Map<String, HttpServlet>
) {
    private val server: Server

    init {
        val threadPool = JettyVirtualThreadPool()

        val config = HttpConfiguration().apply {
            sendServerVersion = false
            sendXPoweredBy = false
            sendDateHeader = false
        }

        val http1 = HttpConnectionFactory(config)
        val http2 = HTTP2CServerConnectionFactory()

        val servletHandler =
            ServletContextHandler(ServletContextHandler.NO_SECURITY + ServletContextHandler.NO_SESSIONS).apply {
                routes.forEach { (path, servlet) ->
                    addServlet(ServletHolder(servlet), path)
                }
            }

        this.server = Server(threadPool).apply {
            handler = GzipHandler(servletHandler)

            // HTTP
            addConnector(
                ServerConnector(server, http1, http2).apply {
                    this.port = port
                }
            )
        }
    }

    fun start() = server.start()

    private class JettyVirtualThreadPool : ThreadPool {
        private val executor = Executors.newVirtualThreadPerTaskExecutor()

        override fun execute(command: Runnable) {
            executor.execute(command)
        }

        override fun join() {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
        }

        override fun getThreads(): Int {
            return 1
        }

        override fun getIdleThreads(): Int {
            return 1
        }

        override fun isLowOnThreads(): Boolean {
            return false
        }
    }

}
