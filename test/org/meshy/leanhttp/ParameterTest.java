package org.meshy.leanhttp;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParameterTest {
    private static Parameter param(String value) {
        return new Parameter("param", "testName", value);
    }

    @Test
    public void testAsBoolean() throws Exception {
        assertTrue(param("TRUE").asBoolean());
        assertTrue(param("yes").asBoolean());
        assertTrue(param("1").asBoolean());
        assertTrue(param("oN").asBoolean());
        assertTrue(param("y").asBoolean());
        assertFalse(param("falSE").asBoolean());
        assertFalse(param("no").asBoolean());
        assertFalse(param("off").asBoolean());
        assertFalse(param("0").asBoolean());
        assertFalse(param("n").asBoolean());
    }

    @Test(expected = ParameterFormatException.class)
    public void testAsBooleanBogus() {
        param("dinosaur").asBoolean();
    }

    @Test
    public void testAsBooleanOrDefault() {
        assertTrue(param("true").asBooleanOrDefault(false));
        assertTrue(param("").asBooleanOrDefault(true));
        assertTrue(param(null).asBooleanOrDefault(true));
    }

    @Test
    public void testGet() {
        assertEquals("hello", param("hello").get());
    }

    @Test(expected = ParameterMissingException.class)
    public void testGetMissing() {
        param(null).get();
    }

    @Test
    public void testAsInt() {
        assertEquals(-42, param("-42").asInt());
    }

    @Test
    public void testAsLong() {
        assertEquals(9223372036854775807L, param("9223372036854775807").asLong());
    }

    @Test
    public void testAsDouble() {
        assertEquals(3.141, param("3.141").asDouble());
    }

    @Test(expected = ParameterFormatException.class)
    public void testAsIntBogus() {
        param("aardvark").asInt();
    }

    @Test(expected = ParameterFormatException.class)
    public void testAsLongBogus() {
        param("aardvark").asLong();
    }

    @Test(expected = ParameterFormatException.class)
    public void testAsDoubleBogus() {
        param("aardvark").asDouble();
    }

    @Test
    public void testAsIntOrNull() {
        assertEquals(Integer.valueOf(-42), param("-42").asIntOrNull());
        assertEquals(null, param(null).asIntOrNull());
        assertEquals(null, param("").asIntOrNull());
    }

    @Test
    public void testAsLongOrNull() {
        assertEquals(Long.valueOf(9223372036854775807L), param("9223372036854775807").asLongOrNull());
        assertEquals(null, param(null).asLongOrNull());
        assertEquals(null, param("").asLongOrNull());
    }

    @Test
    public void testAsDoubleOrNull() {
        assertEquals(Double.valueOf(3.141), param("3.141").asDoubleOrNull());
        assertEquals(null, param(null).asDoubleOrNull());
        assertEquals(null, param("").asDoubleOrNull());
    }

    @Test(expected = ParameterFormatException.class)
    public void testAsIntOrNullBogus() {
        param("aardvark").asIntOrNull();
    }

    @Test(expected = ParameterFormatException.class)
    public void testAsLongOrNullBogus() {
        param("aardvark").asLongOrNull();
    }

    @Test(expected = ParameterFormatException.class)
    public void testAsDoubleOrNullBogus() {
        param("aardvark").asDoubleOrNull();
    }
}
