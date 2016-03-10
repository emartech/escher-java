package com.emarsys.escher.demo;


import com.emarsys.escher.Escher;
import com.emarsys.escher.EscherException;
import com.emarsys.escher.EscherRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.client.utils.URIBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerTest {

    public static void main(String... args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
        server.createContext("/", exchange -> {

            String response;

            try {
                authenticate(exchange);

                response = "Everything is OK\n";

            } catch (EscherException e) {
                response = e.getMessage();
            }

            exchange.sendResponseHeaders(200, response.length());

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

        });
        server.setExecutor(null);
        server.start();
        System.out.println("started");
    }


    private static void authenticate(HttpExchange exchange) throws EscherException {
        EscherRequest request = new MyServerEscherRequest(exchange);
        Escher escher = new Escher("eu/suite/ems_request")
                .setAuthHeaderName("X-Ems-Auth")
                .setDateHeaderName("X-Ems-Date")
                .setAlgoPrefix("EMS");

        Map<String, String> keyDb = new HashMap<>();
        keyDb.put("ACCESS_KEY_ID", "SECRET");

        escher.authenticate(request, keyDb, new InetSocketAddress("localhost", 8888));
    }

}


class MyServerEscherRequest implements EscherRequest {

    private HttpExchange exchange;


    public MyServerEscherRequest(HttpExchange exchange) {
        this.exchange = exchange;
    }


    @Override
    public String getHttpMethod() {
        return exchange.getRequestMethod();
    }


    @Override
    public URI getURI() {
        try {
            return new URIBuilder(exchange.getRequestURI())
                    .setScheme("http")
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<Header> getRequestHeaders() {
        List<Header> headers = new ArrayList<>();
        exchange.getRequestHeaders().forEach((fieldName, fieldValues) ->
                fieldValues.forEach(fieldValue ->
                        headers.add(new Header(fieldName, fieldValue))
                )
        );
        return headers;
    }


    @Override
    public void addHeader(String fieldName, String fieldValue) {
        throw new RuntimeException("Should not be called");
    }


    @Override
    public String getBody() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}