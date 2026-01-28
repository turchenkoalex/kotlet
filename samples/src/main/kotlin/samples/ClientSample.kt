package samples

import jetty.startJettyServer
import kotlet.Kotlet
import kotlet.client.Client
import kotlet.client.ClientOptions
import kotlet.receiveBody
import kotlet.respondJson
import kotlinx.serialization.Serializable
import java.net.URI
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

fun main() {
    val waitForServer = CountDownLatch(1)

    thread(isDaemon = false) {
        waitForServer.await()
        startClientTest()
    }
    startServer(waitForServer)
}

fun startClientTest() {
    println("Starting client test...")

    val client = Client.newClient(
        options = ClientOptions(
            allowGzipRequests = true,
            allowGzipResponses = true
        )
    )

    var counter = 0L
    while (true) {
        val req = SimpleRequest(message = "Hello, Kotlet RPC!", iteration = counter++)

        try {
            val resp: SimpleResponse? = client.post(URI.create("http://localhost:8080/client_sample"), req)
            println("Received response: $resp")
        } catch (expected: Exception) {
            println("Request failed: ${expected.message}")
        }

        try {
            val resp = client.get<SimpleResponse>(URI.create("http://localhost:8080/client_sample"))
            println("Received response: $resp")
        } catch (expected: Exception) {
            println("Request failed: ${expected.message}")
        }

        Thread.sleep(1000)
    }
}

fun startServer(waitForServer: CountDownLatch) {
    val routing = Kotlet.routing {
        get("/client_sample") { call ->
            println("Received GET request")
            val resp = SimpleResponse(reply = "Received GET")
            call.respondJson(resp)
        }

        post("/client_sample") { call ->
            println("Received POST request")
            val req = call.receiveBody<SimpleRequest>()
            val resp = SimpleResponse(reply = "Received POST: $req")
            call.respondJson(resp)
        }
    }

    startJettyServer(
        routing = routing,
        onStart = {
            waitForServer.countDown()
        },
        onShutdown = {
            println("Server is shutting down")
        }
    )

}

@Serializable
data class SimpleRequest(
    val message: String,
    val iteration: Long
)

@Serializable
data class SimpleResponse(
    val reply: String
)
