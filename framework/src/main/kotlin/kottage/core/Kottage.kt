package kottage.core

import io.netty.channel.group.DefaultChannelGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.group.ChannelGroup
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.concurrent.ImmediateEventExecutor
import kottage.core.router.HttpMethod
import kottage.core.router.Route
import kottage.core.router.Router
import java.net.InetSocketAddress
import java.nio.charset.Charset


/**
 * @author Michael Vaughan
 */

class Kottage(val router: Router) {

    private val channelGroup = DefaultChannelGroup(ImmediateEventExecutor.INSTANCE)
    private val eventLoopGroup = NioEventLoopGroup()
    private var channel: Channel? = null

    fun start(address: InetSocketAddress): Unit {
        val bootstrap = ServerBootstrap().group(eventLoopGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(KottageChannelInitializer(channelGroup, router))
        val future = bootstrap.bind(address)
        future.syncUninterruptibly()
        channel = future.channel()
        val server = this
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                server.destroy()
            }
        })
        future.channel().closeFuture().syncUninterruptibly()
    }

    fun destroy() {
        if (channel != null) {
            channel!!.close()
        }
        channelGroup.close()
        eventLoopGroup.shutdownGracefully()
    }

}

class KottageChannelInitializer(val group: ChannelGroup, val router: Router) : ChannelInitializer<Channel>() {

    override fun initChannel(ch: Channel?) {
        if (ch != null) {
            val pipeline = ch.pipeline()
            pipeline.addLast(HttpServerCodec())
            pipeline.addLast(HttpObjectAggregator(64 * 1024))
            pipeline.addLast(ChunkedWriteHandler())
            pipeline.addLast(KottageRequestHandler(router))
        }
    }
}


class KottageRequestHandler(val router: Router) : SimpleChannelInboundHandler<FullHttpRequest>() {

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {

        // If the message or context is null, not exactly sure what to do at this point.
        if (msg == null || ctx == null) {
            throw UnsupportedOperationException()
        }

        val uri = UriBuilder(msg.uri)
        val method = HttpMethod.valueOf(msg.method.name())
        val route = this.router.match(method, uri.path)

        // Convert the content to a byte array.
        val bytebuf = msg.content()
        val bodyBytes = ByteArray(bytebuf.capacity()).apply { bytebuf.readBytes(this) }

        val message: HttpResponse = handleRequest(route, uri, msg.protocolVersion, bodyBytes)

        ctx.write(message)
        val future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
        future.addListener(io.netty.channel.ChannelFutureListener.CLOSE)
    }


    private fun handleRequest(route: Route?, uri: Uri, protocolVersion: HttpVersion, body: ByteArray): HttpResponse {
        return if (route != null) {
            val parameters = extractParameters(uri, route)
            val request = Request(parameters, body)
            val actionResponse = route.action(request)
            val httpResponseStatus = HttpResponseStatus.valueOf(actionResponse.status)
            val byteBuf = Unpooled.copiedBuffer(actionResponse.body, Charset.forName("UTF-8"))
            DefaultFullHttpResponse(protocolVersion, httpResponseStatus, byteBuf).apply {
                this.headers().apply { actionResponse.headers.forEach { add(it.key, it.value) } }
            }
        } else {
            DefaultFullHttpResponse(protocolVersion, HttpResponseStatus.NOT_FOUND)
        }
    }


    private fun extractParameters(uri: Uri, route: Route): Map<String, String> {
        return extractPathParameters(uri.path, route) + extractQueryParameters(uri.queryString)
    }


    private fun extractPathParameters(path: String?, route: Route): MutableMap<String, String> {
        return if (path != null) {
            val splitPath = path.split('/')
            mutableMapOf<String, String>().apply { putAll(route.pathParamMap.map { (it.value to splitPath[it.key]) }) }
        } else mutableMapOf()
    }


    private fun extractQueryParameters(queryParams: String?): MutableMap<String, String> {
        return if (queryParams != null) {
            val params = queryParams.split("&")
                    .map {
                        val split = it.split("=", limit = 2)
                        split[0] to split[1]
                    }
            mutableMapOf<String, String>().apply {
                putAll(params)
            }

        } else mutableMapOf()

    }

}

/**
 * Constructs a Uri instance from a string.
 */
object UriBuilder : (String) -> Uri {
    override fun invoke(uri: String): Uri {

        // Determine the start of the query string by finding the index of the first ?. Then convert the value
        // to null if it is -1 so we can later use null checking.
        val queryStart = uri.indexOf('?').let {
            if (it != -1) it
            else null
        }

        // Determine the start of the hash string by finding the index of # after the first ?. Also convert to
        // null if -1 for null checking.
        val hashStart = uri.indexOf('#', startIndex = queryStart?.plus(1) ?: 0).let {
            if (it != -1) it
            else null
        }

        // Null checking payoff
        val pathEnd = queryStart ?: hashStart ?: uri.length
        val queryEnd = hashStart ?: uri.length

        // Extract the path. If the length of the path is 0, return /.
        val path = if (pathEnd > 0) uri.substring(0, pathEnd) else "/"
        val queryString = if (queryStart != null && (queryEnd - queryStart) > 1) uri.substring(queryStart + 1, queryEnd)
                else null
        val hash = if (hashStart != null && hashStart != uri.lastIndex) uri.substring(hashStart + 1, uri.length)
                else null

        // Build and return the Uri
        return Uri(path, queryString, hash)
    }
}

data class Uri(val path: String, val queryString: String?, val hash: String?)

