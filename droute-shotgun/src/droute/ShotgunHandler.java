package droute;

import org.meshy.jshotgun.Shotgun;

public class ShotgunHandler implements Handler {
	private final Shotgun shotgun = new Shotgun(new Target());
	private final String handlerClass;
	private Handler handler;
	
	public ShotgunHandler(String handlerClass) {
		this.handlerClass = handlerClass;
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
