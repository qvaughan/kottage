package kottage.core.router

import kottage.core.Request
import kottage.core.Response

/**
 * Router implementation for Kottage.
 * @author Michael Vaughan
 */

enum class HttpMethod {
    GET, POST, PUT, DELETE
}

data class Route(val method: HttpMethod, val path: String, val regex: Regex, val pathParamMap: Map<Int, String>, val action: (Request) -> Response)


class Router {

    private val routes: MutableMap<HttpMethod, MutableList<Route>> = mutableMapOf()

    fun clear() {
        routes.clear()
    }

    fun match(method: HttpMethod, path: String): Route? {
        return routes[method]?.firstOrNull {it.regex.matches(path)}
    }

    fun get(path: String, f: (Request) -> Response) : Router {
        addRoute(HttpMethod.GET, path, f)
        return this
    }

    fun post(path: String, f: (Request) -> Response) : Router{
        addRoute(HttpMethod.GET, path, f)
        return this
    }

    fun put(path: String, f: (Request) -> Response) : Router {
        addRoute(HttpMethod.PUT, path, f)
        return this
    }

    fun delete(path: String, f: (Request) -> Response) : Router {
        addRoute(HttpMethod.DELETE, path, f)
        return this
    }

    private fun addRoute(method: HttpMethod, path: String, f: (Request) -> Response) {
        val pathParamMap = mutableMapOf<Int, String>()
        val splitPath = path.split("/")
        val pathPattern = splitPath.mapIndexed { i, section ->
            // If it is a path parameter, it will begin with a colon
            if (section.startsWith(":")) {
                // Update the path param map with the name of the path param and it's index in the path, removing the
                // : prefix
                pathParamMap.put(i, section.substring(1))
                // Return a regex for matching this segment of the path
                "[^/]+"
            } else {
                // Otherwise, replace the * with [^/]+ for regex
                section.replace("*", "[^/]+")
            }
        }.joinToString("/")
        routes.getOrPut(method, {mutableListOf()}).add(Route(method, path, Regex(pathPattern), pathParamMap, f))
    }

}

