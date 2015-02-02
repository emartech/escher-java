package com.emarsys.escher;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class HelperTest {

    @Test
    public void testCanonicalize() throws Exception{
        Map<String, String> headers = new HashMap<>();
        headers.put("Date", "Mon, 09 Sep 2011 23:36:00 GMT");
        headers.put("Host", "host.foo.com");

        Map<String, String> params = new HashMap<>();

        Request request = new Request("GET", headers, "host.foo.com", "/", params, "");

        String canonicalised = Helper.canonicalize(request);

        assertEquals("GET\n/\n\ndate:Mon, 09 Sep 2011 23:36:00 GMT\nhost:host.foo.com\n\ndate;host\ne3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", canonicalised);
    }

}