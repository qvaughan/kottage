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
import kottage.core.router.Router
import java.net.InetSocketAddress
import java.nio.charset.Charset


/**
 * @author Michael Vaughan
 */

class KottageServer (val router: Router) {

    private val channelGroup = DefaultChannelGroup(ImmediateEventExecutor.INSTANCE)
    private val eventLoopGroup = NioEventLoopGroup()
    private var channel: Channel? = null

    fun start(address: InetSocketAddress) : Unit {
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
        if (msg != null && ctx != null) {
            val path = msg.uri.takeWhile { it != '?' }
            val method = HttpMethod.valueOf(msg.method.name())
            val route = this.router.match(method, path)
            val message: HttpResponse = if (route != null) {
                val actionResponse = route.action(Request(mapOf()))
                val httpResponseStatus = HttpResponseStatus.valueOf(actionResponse.status)
                val byteBuf = Unpooled.copiedBuffer(actionResponse.body, Charset.forName("UTF-8"))
                DefaultFullHttpResponse(msg.protocolVersion, httpResponseStatus, byteBuf).apply {
                    headers().apply {
                        actionResponse.headers.forEach { add(it.key, it.value) }
                    }
                }
            } else {
                DefaultFullHttpResponse(msg.protocolVersion, HttpResponseStatus.NOT_FOUND)
            }
            ctx.write(message)
            val future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
            future.addListener(io.netty.channel.ChannelFutureListener.CLOSE)

        } else {
            throw UnsupportedOperationException()
        }
    }



}

