package droute;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;

import droute.legacy.Handler;
import droute.ModelAndView;
import droute.legacy.Request;
import droute.legacy.Response;

public class HandlebarsHandler implements Handler {
	
	private final Handler handler;
	private final Handlebars handlebars;
	private String fileExtension = ".hbs";
	
	public HandlebarsHandler(Handlebars handlebars, Handler handler) {
		this.handlebars = handlebars;
		this.handler = handler;
	}
	
	public HandlebarsHandler(Handler handler) {
		this(new Handlebars(), handler);
	}
	
	@Override
	public Response handle(Request request) {
		Response response = handler.handle(request);
		Object body = response.body();
		if (body instanceof ModelAndView) {
			ModelAndView modelAndView = (ModelAndView)body;
			String view = modelAndView.view();
			if (view.endsWith(fileExtension)) {
				try {
					return response.withBody(handlebars.compile(view).apply(modelAndView.model()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return response;
	}

}
