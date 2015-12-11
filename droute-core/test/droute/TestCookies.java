package droute;

import static org.junit.Assert.*;

import java.util.Map;

import droute.legacy.Cookies;
import org.junit.Test;

public class TestCookies {

	@Test
	public void testParseSIDExample() {
		Map<String,String> cookies = Cookies.parse("SID=31d4d96e407aad42");
		assertEquals(1, cookies.size());
		assertEquals("31d4d96e407aad42", cookies.get("SID"));
	}
	
	@Test
	public void testParseMultipleCookies() {
		Map<String,String> cookies = Cookies.parse("SID=31d4d96e407aad42; excellent=true; DISASTER!=\"NO!\"");
		assertEquals(3, cookies.size());
		assertEquals("31d4d96e407aad42", cookies.get("SID"));
		assertEquals("NO!", cookies.get("DISASTER!"));
	}

	@Test
	public void testEncode() {
		assertEquals("hello=cruel+world", Cookies.encode("hello", "cruel world"));
		assertEquals("hello=world; Secure", Cookies.encode("hello", "world", Cookies.secure()));
		assertEquals("hello=world; Secure; HttpOnly", Cookies.encode("hello", "world", Cookies.secure(), Cookies.httpOnly()));
		assertEquals("hello=world; Secure; HttpOnly; Max-Age=5", Cookies.encode("hello", "world", Cookies.secure(), Cookies.httpOnly(), Cookies.maxAge(5)));
	}

}
