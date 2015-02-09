package com.emarsys.escher;


import java.net.URI;
import java.util.List;

public class RequestImpl implements Request {

    private String httpMethod;
    private URI uri;
    private List<Header> headers;
    private String body;


    public RequestImpl(String httpMethod, URI uri, List<Header> headers, String body) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.headers = headers;
        this.body = body;
    }


    @Override
    public URI getURI() {
        return uri;
    }


    @Override
    public List<Header> getRequestHeaders() {
        return headers;
    }


    @Override
    public String getHttpMethod() {
        return httpMethod;
    }


    @Override
    public void addHeader(String key, String value) {
        this.headers.add(new Header(key, value));
    }


    @Override
    public boolean hasHeader(String key) {
        return this.headers.stream().anyMatch((nameValuePair) -> nameValuePair.getFieldName().equals(key));
    }


    public String getBody() {
        return body;
    }

}
