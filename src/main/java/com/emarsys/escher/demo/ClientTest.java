package com.emarsys.escher.demo;

import com.emarsys.escher.Escher;
import com.emarsys.escher.EscherException;
import com.emarsys.escher.Request;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClientTest {

    public static void main(String... args) {
        try {
            String url = "http://trunk-int.s.emarsys.com/api/v2/internal/215076962/field";

            HttpClient client = HttpClientBuilder.create().build();
            HttpRequestBase request = new HttpGet(url);
            request.addHeader("host", "trunk-int.s.emarsys.com");
            request.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());


            request = signRequest(request);


            HttpResponse response = client.execute(request);

            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
            System.out.println(fetchResponse(response));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static HttpRequestBase signRequest(HttpRequestBase request) throws EscherException {
        EscherRequest escherRequest = new EscherRequest(request, "");

        Escher escher = new Escher("eu/suite/ems_request")
                .setAuthHeaderName("X-Ems-Auth")
                .setDateHeaderName("X-Ems-Date")
                .setAlgoPrefix("EMS");

        escher.signRequest(escherRequest, "[ACCESS_KEY_ID]", "[SECRET]", Arrays.asList("Content-Type", "X-Ems-Date", "host"));

        return escherRequest.getHttpRequest();
    }


    private static String fetchResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        String result = "";
        String line;
        while ((line = rd.readLine()) != null) {
            result += line;
        }
        return result;
    }
}


class EscherRequest implements Request {

    private HttpRequestBase httpRequest;
    private String body;


    public EscherRequest(HttpRequestBase httpRequest, String body) {
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
    public List<Request.Header> getRequestHeaders() {
        return Arrays.asList(httpRequest.getAllHeaders())
                .stream()
                .map(header -> new Request.Header(header.getName(), header.getValue()))
                .collect(Collectors.toList());
    }


    @Override
    public void addHeader(String fieldName, String fieldValue) {
        httpRequest.addHeader(fieldName, fieldValue);
    }


    @Override
    public boolean hasHeader(String fieldName) {
        return httpRequest.getFirstHeader(fieldName) != null;
    }


    @Override
    public String getBody() {
        return body;
    }


    public HttpRequestBase getHttpRequest() {
        return httpRequest;
    }
}