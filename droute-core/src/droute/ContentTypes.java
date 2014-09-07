package droute;

import java.util.HashMap;
import java.util.Map;

public class ContentTypes {
	private static final Map<String,String> types = new HashMap<String,String>();
	
	static {
		types.put("css", "text/css");
		types.put("js", "application/javascript");
		types.put("png", "image/png");
		types.put("gif", "image/gif");
		types.put("jpg", "image/jpeg");
		types.put("woff", "application/font-woff");
		types.put("ttf", "application/font-sfnt");
		types.put("svg", "image/svg+xml");
		types.put("eot", "application/vnd.ms-fontobject");
	}
	
	public static String fromExtension(String filename) {
		int i = filename.lastIndexOf('.') + 1;
		if (i >= 0) {
			String ext = filename.substring(i);
			return types.get(ext);
		}
		return null;
	}
}
