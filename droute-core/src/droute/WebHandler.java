package droute;

import java.io.IOException;

public interface WebHandler extends AutoCloseable {
    WebResponse handle(WebRequest request) throws IOException;

    /**
     * Closes this handler, releasing any held resources.
     *
     * The default implementation does nothing. Handlers which delegate to
     * one or more child handlers must ensure they call close on all children.
     *
     * @throws Exception if this handler could not be closed
     */
    @Override
    default void close() throws Exception {
		// do nothing by default
    }
}
