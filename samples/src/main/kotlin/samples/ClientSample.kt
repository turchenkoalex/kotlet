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
        options = ClientOptions.DEFAULT.copy(allowGzipRequests = true)
    )

    var counter = 0L
    while (true) {
        val req = SimpleRequest(message = "Hello, Kotlet RPC!", iteration = counter++)

        run {
            val resp: SimpleResponse? = client.post(URI.create("http://localhost:8080/post"), req)
            println("Client: $resp")
        }

        run {
            val resp: SimpleResponse? = client.put(URI.create("http://localhost:8080/put"), req)
            println("Client: $resp")
        }

        run {
            val resp = client.get<SimpleResponse>(URI.create("http://localhost:8080/get"))
            println("Client: $resp")
        }

        run {
            val resp = client.delete<SimpleResponse>(URI.create("http://localhost:8080/delete"))
            println("Client: $resp")
        }

        Thread.sleep(1000)
    }
}

fun startServer(waitForServer: CountDownLatch) {
    val routing = Kotlet.routing {
        get("/get") { call ->
            println("Server GET request")
            val resp = SimpleResponse(reply = "GET")
            call.respondJson(resp)
        }

        post("/post") { call ->
            println("Server POST request")
            val req = call.receiveBody<SimpleRequest>()
            val resp = SimpleResponse(reply = "POST: $req")
            call.respondJson(resp)
        }

        put("/put") { call ->
            println("Server PUT request")
            val req = call.receiveBody<SimpleRequest>()
            val resp = SimpleResponse(reply = "PUT: $req")
            call.respondJson(resp)
        }

        delete("/delete") { call ->
            println("Server DELETE request")
            val resp = SimpleResponse(reply = "DELETE")
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
