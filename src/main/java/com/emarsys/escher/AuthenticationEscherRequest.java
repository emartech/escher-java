package com.emarsys.escher;


import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public class AuthenticationEscherRequest implements EscherRequest {
    
    private EscherRequest request;
    private Config config;
    private String host;


    public AuthenticationEscherRequest(EscherRequest request, Config config, String host) {
        this.request = request;
        this.config = config;
        this.host = host;
    }


    @Override
    public String getHttpMethod() {
        return request.getHttpMethod();
    }


    @Override
    public URI getURI() {
        try {
            URIBuilder uriBuilder = new URIBuilder(request.getURI());
            uriBuilder.setHost(host);
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI cannot be modified", e);
        }
    }


    @Override
    public List<Header> getRequestHeaders() {
        return request.getRequestHeaders()
                .stream()
                .filter(header -> !header.getFieldName().equalsIgnoreCase(config.getAuthHeaderName()))
                .collect(Collectors.toList());
    }


    @Override
    public void addHeader(String fieldName, String fieldValue) {
        request.addHeader(fieldName, fieldValue);
    }


    @Override
    public boolean hasHeader(String fieldName) {
        return request.hasHeader(fieldName);
    }


    @Override
    public String getBody() {
        return request.getBody();
    }
}
