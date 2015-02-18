package com.emarsys.escher.acceptance;

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

public class Server {

    private HttpServer server;


    public void start() throws IOException {
        if (server != null) {
            throw new IllegalStateException("server has been already started");
        }

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {

            try {
                authenticate(exchange);
                print(exchange, "OK");
            } catch (Exception e) {
                print(exchange, e.getMessage());
            }

        });
        server.start();
    }


    private void authenticate(HttpExchange exchange) throws EscherException {
        EscherRequest request = new EscherRequestServerImpl(exchange);
        Escher escher = new Escher("test/credential/scope");

        Map<String, String> keyDb = new HashMap<>();
        keyDb.put("ACCESS_KEY_ID", "SECRET");

        escher.authenticate(request, keyDb, new InetSocketAddress("localhost", getPort()));
    }


    private void print(HttpExchange exchange, String message) throws IOException {
        exchange.sendResponseHeaders(200, message.length());

        OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();
    }


    public void stop() {
        server.stop(0);
    }


    public int getPort() {
        return server.getAddress().getPort();
    }


    private class EscherRequestServerImpl implements EscherRequest {

        private HttpExchange exchange;


        public EscherRequestServerImpl(HttpExchange exchange) {
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
            try {
                String body = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                for(String line = br.readLine(); line != null; line = br.readLine()) {
                    body += line + "\n";
                }
                br.close();
                return body;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
