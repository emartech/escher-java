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

}