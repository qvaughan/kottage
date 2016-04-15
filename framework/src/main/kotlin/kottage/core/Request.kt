package kottage.core

/**
 * @author Michael Vaughan
 */
data class Request(val params: Map<String, String>, val body: Body)