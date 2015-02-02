package com.emarsys.escher;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HelperTest {

    @Test
    public void testCanonicalize() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        TestParam param = mapper.readValue(new File("src/test/fixtures/aws4_testsuite/get-vanilla.json"), TestParam.class);
        TestParam.Request paramRequest = param.getRequest();

        List<String[]> headers = new ArrayList<>();
        for (List<String> header : paramRequest.getHeaders()) {
            headers.add(new String[] {header.get(0), header.get(1)});
        }

        Map<String, String> params = new HashMap<>();

        Request request = new Request(paramRequest.getMethod(), headers, paramRequest.getHost(), paramRequest.getUrl(), params, paramRequest.getBody());

        String canonicalised = Helper.canonicalize(request);

        assertEquals(param.getExpected().getCanonicalizedRequest(), canonicalised);
    }

}