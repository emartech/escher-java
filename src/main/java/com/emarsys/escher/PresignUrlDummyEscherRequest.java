package com.emarsys.escher;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

class PresignUrlDummyEscherRequest implements EscherRequest {

    private URI uri;


    public PresignUrlDummyEscherRequest(URI uri) {
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
        return Arrays.asList(new Header("host", calculateHost(uri)));
    }


    private String calculateHost(URI uri) {
        String host = uri.getHost();
        int port = uri.getPort();
        if (port != -1) {
            host += ":" + port;
        }
        return host;
    }


    @Override
    public void addHeader(String fieldName, String fieldValue) {}


    @Override
    public String getBody() {
        return Escher.UNSIGNED_PAYLOAD;
    }
}
