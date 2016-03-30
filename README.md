## Kottage - Quickly build reactive Kotlin microservices
Kottage is an HTTP service framework with emphasis on building reactive microservices in Kotlin. It is heavily influenced by 
the [Play](https://playframework.com/) and [Spark] (http://sparkjava.com/).

## Core Ideas
* Make building reactive services easy.
* Offer configurability, but "just work" out of the box.
* Offer opinionated functionality as modules that can be overridden by the developer when possible.

## Creating a Server that says Hi
```
KottageServer(Router()
  .get("/hello") {
    Response(200, body = "{\"msg\": \"hi\"}")
  }
).start(InetSocketAddress(8888))
```
Now browsing to `localhost:8888/hello` will say: `{"msg": "hi"}`
