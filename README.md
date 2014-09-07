droute
======

Java 8 microwebframework prototype.  Just exploratory, I currently have no intention of maintaining
or properly documenting this.  It takes some obvious inspiration from Compojure and Ring.

Example
-------

```java
import static droute.Response.*;
import static droute.Route.*;
import droute.*;

public class Example {
  Handler routes = routes(
    resources("/webjars", "META-INF/resources/webjars"),
    GET("/", request -> response("Hello world")),
    GET("/things/:id", this::showThing, "id", "[0-9]+"),
    notFound("There's nothing here."),
  );
  
  Response showThing(Request request) {
    return response("showing thing " + request.param("id"));
  }
  
  public static void main(String[] args) {
    Example app = new Example();
    new NanoServer(app.routes, 8080).start();
    System.in.read();
  }
}
```
