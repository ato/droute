package droute.v2;

import java.util.HashMap;
import java.util.Map;

class ContentTypes {
	private static final Map<String,String> contentTypes = new HashMap<String,String>();
	
	static {
		contentTypes.put("css", "text/css");
		contentTypes.put("js", "application/javascript");
		contentTypes.put("png", "image/png");
		contentTypes.put("gif", "image/gif");
		contentTypes.put("jpg", "image/jpeg");
		contentTypes.put("woff", "application/font-woff");
		contentTypes.put("ttf", "application/font-sfnt");
		contentTypes.put("svg", "image/svg+xml");
		contentTypes.put("eot", "application/vnd.ms-fontobject");
	}
	
	public static String fromExtension(String filename) {
		int i = filename.lastIndexOf('.') + 1;
		if (i >= 0) {
			String ext = filename.substring(i);
			return contentTypes.get(ext);
		}
		return null;
	}
}
