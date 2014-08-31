package droute.nanohttpd;

import static droute.Route.*;
import static droute.Response.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import droute.Handler;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class NanoServer extends NanoHTTPD {

	private final Handler handler;

	public NanoServer(Handler handler, int port) {
		super(port);
		this.handler = handler; 
	}
	
	public NanoServer(Handler handler, String hostname, int port) {
		super(hostname, port);
		this.handler = handler; 
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		droute.Response response = handler.handle(new NanoRequest(session));
		return new Response(lookupStatus(response.status()), response.header("content-length"), coerceBody(response.body()));
	}
	
	private static InputStream coerceBody(Object obj) {
		if (obj instanceof InputStream) {
			return (InputStream)obj;
		} else if (obj instanceof String) {
			return new ByteArrayInputStream(((String)obj).getBytes(Charset.forName("utf-8")));
		} else {
			throw new IllegalArgumentException("unable to handle body of type " + obj.getClass());
		}
	}
	
	private static IStatus lookupStatus(int statusCode) {
		for (Status status : Status.values()) {
			if (status.getRequestStatus() == statusCode) {
				return status;
			}
		}
		return new IStatus() {

			@Override
			public int getRequestStatus() {
				return statusCode;
			}

			@Override
			public String getDescription() {
				return "Unknown";
			}
			
		};
	}
	
	public static void main(String[] args) throws IOException {
		Handler app = routes(
				GET("/", req -> response("hello world")),
				GET("/foo/:id", req -> response("foo " + req.param("id")), "id", "[0-9]+")); 
		NanoServer server = new NanoServer(app, 8080);
		server.start();
		System.in.read();
	}

}