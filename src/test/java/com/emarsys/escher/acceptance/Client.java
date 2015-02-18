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

    public String sendRequest(HttpRequestBase request) throws IOException, EscherException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(request);
        return fetchResponse(response);
    }


    private String fetchResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        return rd.lines()
                .reduce((line1, line2) -> line1 + "\n" + line2)
                .orElse("");
    }


    public HttpRequestBase signRequest(HttpRequestBase request) throws EscherException {
        EscherRequestClientImpl escherRequest = new EscherRequestClientImpl(request, "");
        Escher escher = new Escher("test/credential/scope");
        escher.signRequest(escherRequest, "ACCESS_KEY_ID", "SECRET", new ArrayList<>());
        return escherRequest.getHttpRequest();
    }


    private class EscherRequestClientImpl implements EscherRequest {

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
