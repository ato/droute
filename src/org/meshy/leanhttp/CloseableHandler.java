package org.meshy.leanhttp;

/**
 * This interface exists solely to allow both WebHandler and AutoCloseable to be implemented by an anonymous inner
 * class.
 */
public interface CloseableHandler extends HttpHandler, AutoCloseable {
}
