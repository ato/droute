package droute;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.meshy.jshotgun.Shotgun;

public class ShotgunHandler implements Handler {
	/*
	 * Blacklist the core interfaces as they can't be reloaded without also
	 * reloading the web server.
	 */
	private static final Set<String> blacklist = new HashSet<>(Arrays.asList(
			"droute.Handler",
			"droute.Request",
			"droute.Response",
			"droute.ModelAndView",
			"droute.Streamable"));
	private final Shotgun shotgun = new Shotgun(new Target(), blacklist);
	private final String handlerClass;
	private Handler handler;
	
	public ShotgunHandler(String handlerClass) {
		this.handlerClass = handlerClass;
		shotgun.setUp();
	}
	
	private class Target implements Shotgun.Target {

		@Override
		public void start(ClassLoader classLoader) {
			try {
				handler = (Handler) classLoader.loadClass(handlerClass).newInstance();
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void stop() {
			handler = null;
		}
		
	}

	@Override
	public Response handle(Request request) {
		try {
			shotgun.lock();
			return handler.handle(request);
		} finally {
			shotgun.unlock();
		}
	}
}
