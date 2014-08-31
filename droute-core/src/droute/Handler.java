package droute;

public interface Handler {
	public static final Response NEXT = new Response.Impl(0, null, null);

	Response handle(Request request);
}
