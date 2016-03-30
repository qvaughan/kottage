package kottage.core.router

import kottage.core.Request
import kottage.core.Response
import kottage.core.ResponseBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

/**
 * @author Michael Vaughan
 */

class RouterTest {

    val fooAction = {r: Request ->
        ResponseBuilder(200)
            .header("Content-Type" to "text/plain")
            .body("foo")
            .build()
    }
    val barAction = {r: Request -> Response(200, body = "bar") }
    val bazAction = {r: Request -> Response(200, body = "baz") }
    var router: Router = Router()

    @Before
    fun before() {
        router.clear()
    }

    @Test
    fun testBasicRouteMatching() {
        router.get("/foo", fooAction)
        router.get("/bar", barAction)
        router.get("/baz", bazAction)
        assertThat(router.match(HttpMethod.GET, "/foo")?.action).isEqualTo(fooAction)
        assertThat(router.match(HttpMethod.GET, "/bar")?.action).isEqualTo(barAction)
        assertThat(router.match(HttpMethod.GET, "/baz")?.action).isEqualTo(bazAction)
        assertThat(router.match(HttpMethod.GET, "/dne")?.action).isNull()
    }


    @Test
    fun testPathParamMatching() {
        router.get("/foo/:id", fooAction)
        assertThat(router.match(HttpMethod.GET, "/foo/37")?.action).isEqualTo(fooAction)
        assertThat(router.match(HttpMethod.GET, "/foo")?.action).isNull()
        assertThat(router.match(HttpMethod.GET, "/foo/")?.action).isNull()
        assertThat(router.match(HttpMethod.GET, "/foo/37/")?.action).isNull()
        assertThat(router.match(HttpMethod.GET, "/foo/37/77")?.action).isNull()
    }

}