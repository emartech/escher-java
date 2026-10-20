package com.emarsys.escher.acceptance;


import com.emarsys.escher.Escher;
import com.emarsys.escher.EscherException;
import com.emarsys.escher.EscherRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Client {

    private static final String ESCHER_ACCESS_KEY_ID = "ACCESS_KEY_ID";
    private static final String ESCHER_SECRET = "SECRET";

    private final StubClock clock = new StubClock();

    private Escher escher = new Escher("test/credential/scope", clock);


    public String sendRequest(HttpUriRequestBase request) throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            return client.execute(request, response -> {
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                    return rd.lines().collect(Collectors.joining("\n"));
                }
            });
        }
    }


    public HttpUriRequestBase signRequest(HttpUriRequestBase request) throws EscherException {
        EscherRequestClientImpl escherRequest = new EscherRequestClientImpl(request, "");
        escher.signRequest(escherRequest, ESCHER_ACCESS_KEY_ID, ESCHER_SECRET, new ArrayList<>());
        return escherRequest.getHttpRequest();
    }


    public String presignUrl(String url, int expires) throws EscherException {
        return escher.presignUrl(url, ESCHER_ACCESS_KEY_ID, ESCHER_SECRET, expires);
    }


    public String presignUrl(String url) throws EscherException {
        return escher.presignUrl(url, ESCHER_ACCESS_KEY_ID, ESCHER_SECRET);
    }


    public Escher getEscher() {
        return escher;
    }

    public StubClock getClock() {
        return clock;
    }


    private static class EscherRequestClientImpl implements EscherRequest {

        private HttpUriRequestBase httpRequest;
        private String body;


        public EscherRequestClientImpl(HttpUriRequestBase httpRequest, String body) {
            this.httpRequest = httpRequest;
            this.body = body;
        }


        @Override
        public String getHttpMethod() {
            return httpRequest.getMethod();
        }


        @Override
        public URI getURI() {
            try {
                return httpRequest.getUri();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }


        @Override
        public List<Header> getRequestHeaders() {
            return Arrays.asList(httpRequest.getHeaders())
                    .stream()
                    .map(header -> new EscherRequest.Header(header.getName(), header.getValue()))
                    .collect(Collectors.toList());
        }


        @Override
        public void addHeader(String fieldName, String fieldValue) {
            httpRequest.addHeader(fieldName, fieldValue);
        }


        @Override
        public String getBody() {
            return body;
        }


        public HttpUriRequestBase getHttpRequest() {
            return httpRequest;
        }

    }
}
