package com.emarsys.escher;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

class PresignUrlDummyRequest implements Request {

    private URI uri;


    public PresignUrlDummyRequest(URI uri) {
        this.uri = uri;
    }


    @Override
    public String getHttpMethod() {
        return "GET";
    }


    @Override
    public URI getURI() {
        return uri;
    }


    @Override
    public List<Header> getRequestHeaders() {
        return Arrays.asList(new Header("host", uri.getHost()));
    }


    @Override
    public void addHeader(String fieldName, String fieldValue) {}


    @Override
    public boolean hasHeader(String fieldName) {
        return fieldName.equals("host");
    }


    @Override
    public String getBody() {
        return Escher.UNSIGNED_PAYLOAD;
    }
}
