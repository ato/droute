package droute;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class FreeMarkerHandler implements Handler {
	private final static Charset UTF8 = Charset.forName("utf-8");

	private final Configuration config;
	private final Handler handler;

	public static Configuration defaultConfiguration(Class<?> clazz, String pathPrefix) {
		Configuration config = new Configuration();
		config.setOutputEncoding("utf-8");
		config.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
		config.setIncompatibleImprovements(new Version(2, 3, 20));
		config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		config.setTemplateLoader(new ClassTemplateLoader(clazz, pathPrefix) {

			@Override
			public Reader getReader(Object templateSource, String encoding)
					throws IOException {
				return new SequenceReader(
						new StringReader("[#escape x as x?xhtml]"),
						super.getReader(templateSource, encoding),
						new StringReader("[/#escape]"));
			}
			
		});
		return config;
	}

	public FreeMarkerHandler(Handler handler) {
		this(FreeMarkerHandler.class, "", handler);
	}
	
	public FreeMarkerHandler(Class<?> clazz, String pathPrefix, Handler handler) {
		this(defaultConfiguration(clazz, pathPrefix), handler);
	}

	public FreeMarkerHandler(Configuration config, Handler handler) {
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
			if (response.header("Content-Type") == null) {
				response = response.withHeader("Content-Type", "text/html");
			}
			return response.withBody((Streamable) out -> {
				Writer writer = new OutputStreamWriter(out, UTF8);
				try {
					if (modelAndView.model() instanceof Map) {
						((Map)modelAndView.model()).put("request", request);
					}
					template.process(modelAndView.model(), writer);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
		return response;
	}

	/**
	 * Concatenates a sequence of readers.
	 */
	private static class SequenceReader extends Reader {
		private final Reader[] readers;
		private int i = 0;
		
		SequenceReader(Reader... readers) {
			this.readers = readers;
		}
		
		@Override
		public void close() throws IOException {
			for (; i < readers.length; i++) {
				readers[i].close();
			}
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			for (; i < readers.length; i++) {
				int n = readers[i].read(cbuf, off, len);
				if (n >= 0) {
					return n;
				}
				readers[i].close();
			}
			return -1;
		}
	}
}
