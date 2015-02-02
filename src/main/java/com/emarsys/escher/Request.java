package com.emarsys.escher;


import java.util.List;
import java.util.Map;

public class Request {

    private String httpMethod;
    private List<String[]> headers;
    private String host;
    private String path;
    private Map<String, String> queryParameters;
    private String body;

    public Request(String httpMethod, List<String[]> headers, String host, String path, Map<String, String> queryParameters, String body) {
        this.httpMethod = httpMethod;
        this.headers = headers;
        this.host = host;
        this.path = path;
        this.queryParameters = queryParameters;
        this.body = body;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<String[]> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String[]> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, String value) {
        this.headers.add(new String[]{key, value});
    }

    public boolean hasHeader(String key) {
        return this.headers.stream().anyMatch((array) -> array[0].equals(key));
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
