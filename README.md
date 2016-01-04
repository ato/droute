LeanHttp
======

LeanHttp is a small (~40 KB) library for writing web applications and
web services and provides the following functionality:

* Minimal but convenient request, response and handler abstractions
* Pattern-based URL router with optional regex matching
* Two deployment options
  * Builtin HTTP/1.1 server (one thread per request)
  * Adapter for Java servlet containers (Jetty, Tomcat etc)

Guiding principles:

* A composable library, not a framework
* Low footprint and zero required dependencies
* Easy to use without hidden complexity


Usage
-----

```java
import org.meshy.leanhttp.*;
import static org.meshy.leanhttp.WebResponses.*;
import static org.meshy.leanhttp.WebResponses.*;

HttpRouter router = new HttpRouter();
router.on(GET, "/", request -> ok("Hello world!"));

HttpServer server = new HttpServer(router, new ServerSocket(8080));
server.serve();
```
