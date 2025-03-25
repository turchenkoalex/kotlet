package kotlet.jetty

import kotlet.Routing
import org.eclipse.jetty.server.Server

fun jetty(
    vararg routing: Routing,
    configure: JettyBuilder.() -> Unit
): Server {
    val builder = JettyBuilder()
    configure(builder)
    return builder.build(routing.toList())
}
