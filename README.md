## Kottage - Quickly build reactive Kotlin microservices
Kottage is an HTTP service framework with emphasis on building reactive microservices in Kotlin. It is heavily influenced by 
the [Play](https://playframework.com/) and [Spark] (http://sparkjava.com/).

## Core Ideas
* Make building reactive services easy.
* Offer configurability, but "just work" out of the box.
* Offer opinionated functionality as modules that can be overridden by the developer when possible.

## Creating a Basic Service
```kotlin
Kottage(Router()
  .get("/hello/:id") { request ->
    ResponseBuilder(200)
      .header("Content-Type" to "application/json")
      // return the id path param and a query string param named foo in the response body as JSON
      .body("""{"id": "${request.params["id"]}", "foo": "${request.params["foo"]}}""")
      .build()
  }
).start(InetSocketAddress(8888))
```
Now browsing to `localhost:8888/hello` will say: `{"msg": "hi"}`
