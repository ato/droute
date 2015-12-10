package droute.v2;

import droute.Response;

public interface Handler {
	Response handle(Request request);
}
