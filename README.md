droute
======

droute is a small library for web applications and web services.

```java
WebRouter router = new WebRouter();
router.on("GET", "/", request -> ok("Hello world!"));

HttpServer server = new HttpServer(router, 8080);
server.serve();
```

Functionality:

* A composable request, response and handler abstraction
* A pattern-based URL router with optional regex matching
* Deployment options:
  * A builtin simple but useful HTTP/1.1 server
  * Adapter for Java servlet containers (Jetty, Tomcat etc)

Guiding principles:

* A library not a framework
* Low footprint and zero required dependencies
* Easy to use without hidden complexity

Core Abstraction
----------------

URL Router
----------

Builtin Webserver
-----------------

Deploying to a Servlet Container
--------------------------------

