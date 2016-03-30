package kottage.core.server

import kottage.core.KottageServer
import kottage.core.Response
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
        val router = Router()
                .get("/hello") { Response(200, mutableMapOf("Content-Type" to "application/json; utf-8"),  "{\"msg\": \"hi\"}") }
        val server = KottageServer(router)
        val future = server.start(InetSocketAddress(8888))
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                server.destroy()
            }
        })
        future.channel().closeFuture().syncUninterruptibly()
    }

}