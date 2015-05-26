package com.emarsys.escher;

import java.net.URI;
import java.util.List;

class PresignUrlEscherRequestWrapper implements EscherRequest {

    private final EscherRequest request;


    public PresignUrlEscherRequestWrapper(EscherRequest request) {
        this.request = request;
    }


    @Override
    public String getHttpMethod() {
        return request.getHttpMethod();
    }


    @Override
    public URI getURI() {
        return request.getURI();
    }


    @Override
    public List<Header> getRequestHeaders() {
        return request.getRequestHeaders();
    }


    @Override
    public void addHeader(String fieldName, String fieldValue) { request.addHeader(fieldName, fieldValue); }


    @Override
    public String getBody() {
        return Escher.UNSIGNED_PAYLOAD;
    }
}
