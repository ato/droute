package droute.nanohttpd;

import static droute.Response.response;
import static droute.Route.GET;
import static droute.Route.routes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import droute.Handler;
import droute.Streamable;
import droute.nanohttpd.NanoHTTPD.Response.IStatus;
import droute.nanohttpd.NanoHTTPD.Response.Status;

public class NanoServer extends NanoHTTPD {

	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final Handler handler;
	private static final Logger logger = Logger.getLogger(NanoServer.class.getName());

	public NanoServer(Handler handler, int port) {
		super(port);
		this.handler = handler; 
	}
	
	public NanoServer(Handler handler, String hostname, int port) {
		super(hostname, port);
		this.handler = handler; 
	}

	public NanoServer(Handler handler, ServerSocket serverSocket) {
		super(serverSocket);
		this.handler = handler; 
	}

	
	@Override
	public Response serve(IHTTPSession session) {
		Map<String,String> files = new HashMap<>();
		/*if ("application/x-www-form-urlencoded".equalsIgnoreCase(session
				.getHeaders().get("content-type"))) {*/
			try {
				session.parseBody(files);
			} catch (IOException ioe) {
				StringWriter sw = new StringWriter();
				ioe.printStackTrace(new PrintWriter(sw));
				return new Response(Response.Status.INTERNAL_ERROR,
						MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: "
								+ ioe.getMessage() + "\n\n" + sw);
			} catch (ResponseException re) {
				return new Response(re.getStatus(), MIME_PLAINTEXT,
						re.getMessage());
			}
		//}
		droute.Response response = handler.handle(new NanoRequest(session, files));
		InputStream body = streamify(response.body());
		Response nanoResponse = new Response(lookupStatus(response.status()), body);
		for (Entry<String, String> entry : response.headers().entrySet()) {
			nanoResponse.addHeader(entry.getKey(), entry.getValue());
		}
		if (response.header("Content-Length") != null || body instanceof ByteArrayInputStream) {
			nanoResponse.setChunkedTransfer(false);			
		} else {
			nanoResponse.setChunkedTransfer(true);
		}
		return nanoResponse;
	}
	
	private static Throwable rootCause(Throwable t) {
		while (t.getCause() != null) {
			t = t.getCause();
		}
		return t;
	}
	
	static int i = 0;
	
	private InputStream streamify(Object obj) {
		if (obj instanceof InputStream) {
			return (InputStream)obj;
		} else if (obj == null) {
			return new ByteArrayInputStream(new byte[0]);
		} else if (obj instanceof Streamable) {
			PipedInputStream in = new PipedInputStream(16*1024);
			PipedOutputStream out;
			try {
				out = new PipedOutputStream(in);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
			threadPool.execute(() -> {
				try {
					try {
						((Streamable)obj).writeTo(out);
					} finally {
						out.close();
					}
				} catch (Throwable e) {
					if ("Read end dead".equals(rootCause(e).getMessage())) {
						logger.log(Level.FINE, "Client likely closed connection", e);
					} else {
						logger.log(Level.SEVERE, "Exception streaming output from " + obj, e);
					}
				}
			});
			return in;
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
				GET("/streaming", req -> response((Streamable)out -> {
					try {
						for (int i = 0; i < 1000; i++) {
							out.write(("Loop " + i + "\n").getBytes());
							Thread.sleep(50);
						}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			})	),
				GET("/foo/:id", req -> response("foo " + req.param("id")), "id", "[0-9]+")); 
		NanoServer server = new NanoServer(app, 8080);
		server.startAndJoin();
	}
}