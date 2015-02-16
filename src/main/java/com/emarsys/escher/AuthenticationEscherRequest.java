package com.emarsys.escher;


import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class AuthenticationEscherRequest implements EscherRequest {
    
    private EscherRequest request;
    private String host;


    public AuthenticationEscherRequest(EscherRequest request, String host) {
        this.request = request;
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
        return request.getRequestHeaders();
    }


    @Override
    public void addHeader(String fieldName, String fieldValue) {
        request.addHeader(fieldName, fieldValue);
    }


    @Override
    public String getBody() {
        return request.getBody();
    }
}
