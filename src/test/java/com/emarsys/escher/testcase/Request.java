package com.emarsys.escher.testcase;

import com.emarsys.escher.EscherRequest;

import java.util.List;
import java.util.stream.Collectors;

public class Request {
    private String method;
    private String url;
    private List<List<String>> headers;
    private String body;
    private int expires;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<List<String>> getHeaders() {
        return headers;
    }

    public List<EscherRequest.Header> getHeadersAsHeaders() {
        return headers
                .stream()
                .map(data -> new EscherRequest.Header(
                        data.get(0),
                        data.get(1))
                )
                .collect(Collectors.toList());
    }

    public void setHeaders(List<List<String>> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }
}
