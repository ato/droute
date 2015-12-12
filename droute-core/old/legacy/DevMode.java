package droute.legacy;

import static droute.legacy.Response.response;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DevMode {
	public static Handler prettyErrors(Handler handler) {
		return request -> {
			try {
				return handler.handle(request);
			} catch (Throwable t) {
				t.printStackTrace();
				return Response.response(500, renderError(t));
			}
		};
	}

	private static String renderError(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		StringBuilder sb = new StringBuilder();
		return "<!doctype html><h1>Internal Server Error</h1><pre>" + sw.toString() + "\n\n" + sb.toString();
	}
}
