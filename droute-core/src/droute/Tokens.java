package droute;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Random 120-bit (20 character) base 64 tokens for session ids and the like.  
 */
public class Tokens {
	private Tokens() {}
	
	private static final Pattern RE_SANE_TOKEN = Pattern.compile("[a-zA-Z0-9_-]{20}"); 	
	private static final SecureRandom random = new SecureRandom();
	
	public static String generate() {
		byte[] bytes = new byte[15];
		random.nextBytes(bytes);
		return Base64.getUrlEncoder().encodeToString(bytes);
	}
	
	public static boolean isSane(String token) {
		return token != null && RE_SANE_TOKEN.matcher(token).matches();
	}

}
