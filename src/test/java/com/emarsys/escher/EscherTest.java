package com.emarsys.escher;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class EscherTest extends TestBase {

    @Test
    public void testSignRequest() throws Exception {
        TestParam param = parseTestData("get-vanilla");

        RequestImpl request = createRequest(param.getRequest());

        TestParam.Config config = param.getConfig();

        Escher escher = new Escher(config.getCredentialScope())
                .setAuthHeaderName(config.getAuthHeaderName())
                .setDateHeaderName(config.getDateHeaderName())
                .setAlgoPrefix(config.getAlgoPrefix())
                .setCurrentTime(getConfigDate(param));

        Request signedRequest = escher.signRequest(request, config.getAccessKeyId(), config.getApiSecret(), param.getHeadersToSign());

        RequestImpl expectedSignedRequest = createRequest(param.getExpected().getRequest());
        assertEquals("host", expectedSignedRequest.getURI().getHost(), signedRequest.getURI().getHost());
        assertEquals("method", expectedSignedRequest.getHttpMethod(), signedRequest.getHttpMethod());
        assertEquals("path", expectedSignedRequest.getURI().getPath(), signedRequest.getURI().getPath());
        assertEquals("queryParams", expectedSignedRequest.getURI().getQuery(), signedRequest.getURI().getQuery());
        assertEquals("body", expectedSignedRequest.getBody(), signedRequest.getBody());
        assertEquals("headers", expectedSignedRequest.getRequestHeaders(), signedRequest.getRequestHeaders());
    }


    private RequestImpl createRequest(TestParam.Request paramRequest) throws URISyntaxException {
        List<Request.Header> headers = paramRequest.getHeaders()
                .stream()
                .map(header -> new Request.Header(header.get(0), header.get(1)))
                .collect(Collectors.toList());

        URI uri = new URI("http://" + paramRequest.getHost() + paramRequest.getUrl());

        return new RequestImpl(paramRequest.getMethod(), uri, headers, paramRequest.getBody());
    }


    @Test
    public void testPresignUrl() throws Exception {
        Escher escher = new Escher("us-east-1/host/aws4_request")
                .setAlgoPrefix("EMS")
                .setVendorKey("EMS")
                .setAuthHeaderName("X-Ems-Auth")
                .setDateHeaderName("X-Ems-Date")
                .setCurrentTime(createDate(2011, Calendar.MAY, 11, 12, 0, 0));

        int expires = 123456;
        String signedUrl = escher.presignUrl("http://example.com/something?foo=bar&baz=barbaz", "th3K3y", "very_secure", expires);

        String expectedSignedUrl = "http://example.com/something?foo=bar&baz=barbaz&X-EMS-Algorithm=EMS-HMAC-SHA256&X-EMS-Credentials=th3K3y%2F20110511%2Fus-east-1%2Fhost%2Faws4_request&X-EMS-Date=20110511T120000Z&X-EMS-Expires=123456&X-EMS-SignedHeaders=host&X-EMS-Signature=fbc9dbb91670e84d04ad2ae7505f4f52ab3ff9e192b8233feeae57e9022c2b67";
        assertEquals(expectedSignedUrl, signedUrl);
    }
}