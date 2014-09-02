package droute;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class FreemarkerHandler implements Handler {
	private final static Charset UTF8 = Charset.forName("utf-8");

	private final Configuration config;
	private final Handler handler;

	private static Configuration defaultConfiguration(Class<?> clazz, String pathPrefix) {
		Configuration config = new Configuration();
		config.setOutputEncoding("utf-8");
		config.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
		config.setIncompatibleImprovements(new Version(2, 3, 20));
		config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		config.setClassForTemplateLoading(clazz, pathPrefix);
		return config;
	}

	public FreemarkerHandler(Handler handler) {
		this(FreemarkerHandler.class, "", handler);
	}
	
	public FreemarkerHandler(Class<?> clazz, String pathPrefix, Handler handler) {
		this(defaultConfiguration(clazz, pathPrefix), handler);
	}

	public FreemarkerHandler(Configuration config, Handler handler) {
		this.config = config;
		this.handler = handler;
	}

	@Override
	public Response handle(Request request) {
		Response response = handler.handle(request);
		Object body = response.body();
		if (body instanceof ModelAndView) {
			ModelAndView modelAndView = (ModelAndView) body;
			Template template;
			try {
				template = config.getTemplate(modelAndView.view());
			} catch (IOException e1) {
				throw new RuntimeException("Loading view \"" + modelAndView.view() + "\": " + e1.getMessage(), e1);
			}
			return response.withBody((Streamable) out -> {
				Writer writer = new OutputStreamWriter(out, UTF8);
				try {
					template.process(modelAndView.model(), writer);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
		return response;
	}

}
