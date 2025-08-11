package com.emarsys.escher.testcase;

public class Expected {
    private Request request;
    private String canonicalizedRequest;
    private String stringToSign;
    private String authHeader;
    private String error;
    private String apiKey;
    private String url;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public String getCanonicalizedRequest() {
        return canonicalizedRequest;
    }

    public void setCanonicalizedRequest(String canonicalizedRequest) {
        this.canonicalizedRequest = canonicalizedRequest;
    }

    public String getStringToSign() {
        return stringToSign;
    }

    public void setStringToSign(String stringToSign) {
        this.stringToSign = stringToSign;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
