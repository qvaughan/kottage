package kottage.core

/**
 * @author Michael Vaughan
 */

data class Response(val status: Int, val headers: MutableMap<String, String> = mutableMapOf(), val body: ByteArray? = null)


class ResponseBuilder(private val status: Int) {

    private val headers: MutableMap<String, String> = mutableMapOf()
    private var body: ByteArray? = null

    fun header(vararg headers: Pair<String, String>): ResponseBuilder {
        this.headers.putAll(headers)
        return this
    }

    fun body(body: ByteArray): ResponseBuilder {
        this.body = body;
        return this;
    }

    fun body(body: String): ResponseBuilder {
        this.body = body.toByteArray()
        return this
    }

    fun build(): Response {
        return Response(status, headers, body)
    }

}