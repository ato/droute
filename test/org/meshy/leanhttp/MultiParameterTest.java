package org.meshy.leanhttp;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MultiParameterTest {
    private static MultiParameter param(String... values) {
        return new MultiParameter("param", "testName", Arrays.asList(values));
    }

    @Test
    public void testOrElse() throws Exception {
        assertEquals("hello", param("hello").orElse("otherwise"));
        assertEquals("otherwise", param().orElse("otherwise"));
        assertEquals("otherwise", new MultiParameter("param", "testName", null).orElse("otherwise"));
    }

    @Test(expected = ParameterAmbiguousException.class)
    public void testOrElseAmbiguous() throws Exception {
        param("one", "two").orElse("otherwise");
    }

    @Test
    public void testAsList() throws Exception {
        assertEquals(Collections.emptyList(), param().asList());
        assertEquals(Arrays.asList("one", "two", "three"), param("one", "two", "three").asList());
    }
}