package com.emarsys.escher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EscherTest {

    @Test
    public void testSignRequest() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        TestParam param = mapper.readValue(new File("src/test/fixtures/aws4_testsuite/get-vanilla.json"), TestParam.class);

        Request request = createRequest(param.getRequest());

        TestParam.Config config = param.getConfig();

        Escher escher = new Escher(config.getCredentialScope())
                .setAuthHeaderName(config.getAuthHeaderName())
                .setAlgoPrefix(config.getAlgoPrefix())
                .setCurrentTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(config.getDate()));
        Request signedRequest = escher.signRequest(request, config.getAccessKeyId(), config.getApiSecret(), param.getHeadersToSign());

        Request expectedSignedRequest = createRequest(param.getExpected().getRequest());
        assertEquals("host", expectedSignedRequest.getHost(), signedRequest.getHost());
        assertEquals("method", expectedSignedRequest.getHttpMethod(), signedRequest.getHttpMethod());
        assertEquals("path", expectedSignedRequest.getPath(), signedRequest.getPath());
        assertEquals("queryParams", expectedSignedRequest.getQueryParameters(), signedRequest.getQueryParameters());
        assertEquals("body", expectedSignedRequest.getBody(), signedRequest.getBody());
        assertEquals("headers", expectedSignedRequest.getHeaders(), signedRequest.getHeaders());
    }


    private Request createRequest(TestParam.Request paramRequest) throws URISyntaxException {
        List<NameValuePair> headers = new ArrayList<>();
        for (List<String> header : paramRequest.getHeaders()) {
            headers.add(new BasicNameValuePair(header.get(0), header.get(1)));
        }

        URI uri = new URI("http://" + paramRequest.getHost() + paramRequest.getUrl());

        return new Request(paramRequest.getMethod(), uri, headers, paramRequest.getBody());
    }


    @Test
    public void testPresignUrl() throws Exception {
        Escher escher = new Escher("us-east-1/host/aws4_request")
                .setAlgoPrefix("EMS")
                .setVendorKey("EMS")
                .setAuthHeaderName("X-Ems-Auth")
                .setDateHeaderName("X-Ems-Date")
                .setCurrentTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2011/05/11 12:00:00"));;

        int expires = 123456;
        String signedUrl = escher.presignUrl("http://example.com/something?foo=bar&baz=barbaz", "th3K3y", "very_secure", expires);

        String expectedSignedUrl = "http://example.com/something?foo=bar&baz=barbaz&X-EMS-Algorithm=EMS-HMAC-SHA256&X-EMS-Credentials=th3K3y%2F20110511%2Fus-east-1%2Fhost%2Faws4_request&X-EMS-Date=20110511T120000Z&X-EMS-Expires=123456&X-EMS-SignedHeaders=host&X-EMS-Signature=fbc9dbb91670e84d04ad2ae7505f4f52ab3ff9e192b8233feeae57e9022c2b67";
        assertEquals(expectedSignedUrl, signedUrl);
    }
}