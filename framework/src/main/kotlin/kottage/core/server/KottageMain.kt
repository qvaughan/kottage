package kottage.core.server

import kottage.core.Kottage

import kottage.core.ResponseBuilder
import kottage.core.router.Router
import java.net.InetSocketAddress

/**
 * @author Michael Vaughan
 */
object KottageMain {

    /**
     * This is only for example and testing purposes during development.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        Kottage(Router()
                .get("/hello/:id") { request ->
                    ResponseBuilder(200)
                            .header("Content-Type" to "application/json")
                            .body("""{"id": "${request.params["id"]}", "foo": "${request.params["foo"]}}""")
                            .build()
                }
                .post("/echo") { request ->
                    ResponseBuilder(200)
                            .header("Content-Type" to "application/json")
                            .body(request.body.data)
                            .build()
                }
        ).start(InetSocketAddress(8888))

    }

}