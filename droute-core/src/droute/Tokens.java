package droute;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Random 120-bit (20 character) base 64 tokens for session ids and the like.  
 */
class Tokens {
	private Tokens() {}
	
	private static final Pattern RE_SANE_TOKEN = Pattern.compile("[a-zA-Z0-9_-]{20}"); 	
	private static final SecureRandom random;
	
	static {
		try {
			random = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	static String generate() {
		byte[] bytes = new byte[15];
		random.nextBytes(bytes);
		return Base64.getUrlEncoder().encodeToString(bytes);
	}
	
	static String sanitize(String token) {
		return RE_SANE_TOKEN.matcher(token).matches() ? token : null;
	}

}
