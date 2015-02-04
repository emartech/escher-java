package com.emarsys.escher;


import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.List;

public class Request {

    private String httpMethod;
    private List<NameValuePair> headers;
    private String host;
    private String path;
    private List<NameValuePair> queryParameters;
    private String body;


    public Request(String httpMethod, URI uri, List<NameValuePair> headers, String body) {
        this(httpMethod, headers, uri.getHost(), uri.getRawPath(), URLEncodedUtils.parse(uri, "utf-8"), body);
    }


    public Request(String httpMethod, List<NameValuePair> headers, String host, String path,  List<NameValuePair> queryParameters, String body) {
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

    public List<NameValuePair> getHeaders() {
        return headers;
    }

    public void setHeaders(List<NameValuePair> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, String value) {
        this.headers.add(new BasicNameValuePair(key, value));
    }

    public boolean hasHeader(String key) {
        return this.headers.stream().anyMatch((nameValuePair) -> nameValuePair.getName().equals(key));
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

    public List<NameValuePair> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(List<NameValuePair> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
