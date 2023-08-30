package com.emarsys.escher.acceptance;


import com.emarsys.escher.Escher;
import com.emarsys.escher.EscherException;
import com.emarsys.escher.EscherRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Client {

    private static final String ESCHER_ACCESS_KEY_ID = "ACCESS_KEY_ID";
    private static final String ESCHER_SECRET = "SECRET";

    private final StubClock clock = new StubClock();

    private Escher escher = new Escher("test/credential/scope", clock);


    public String sendRequest(HttpRequestBase request) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(request);
        return fetchResponse(response);
    }


    private String fetchResponse(HttpResponse response) throws IOException {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            return rd.lines().collect(Collectors.joining("\n"));
        }
    }


    public HttpRequestBase signRequest(HttpRequestBase request) throws EscherException {
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

        private HttpRequestBase httpRequest;
        private String body;


        public EscherRequestClientImpl(HttpRequestBase httpRequest, String body) {
            this.httpRequest = httpRequest;
            this.body = body;
        }


        @Override
        public String getHttpMethod() {
            return httpRequest.getMethod();
        }


        @Override
        public URI getURI() {
            return httpRequest.getURI();
        }


        @Override
        public List<Header> getRequestHeaders() {
            return Arrays.asList(httpRequest.getAllHeaders())
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


        public HttpRequestBase getHttpRequest() {
            return httpRequest;
        }

    }
}
