package kottage.core

/**
 * @author Michael Vaughan
 */

data class Response(val statusCode: Int, val headers: MutableMap<String, String> = mutableMapOf(), val body: String? = null)