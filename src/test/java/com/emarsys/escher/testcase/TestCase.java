package com.emarsys.escher.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestCase {
    public static final String TEST_CASES_FOLDER = "test-cases";
    public static final String EXCLUDED_FOLDER = ".conflict";

    private String title;
    private String description;
    private Request request;
    private Config config;
    private List<String> headersToSign;
    private List<String> mandatorySignedHeaders = new ArrayList<>();
    private List<List<String>> keyDb;
    private Expected expected;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public List<String> getHeadersToSign() {
        return headersToSign;
    }

    public void setHeadersToSign(List<String> headersToSign) {
        this.headersToSign = headersToSign;
    }

    public List<String> getMandatorySignedHeaders() {
        return mandatorySignedHeaders;
    }

    public void setMandatorySignedHeaders(List<String> mandatorySignedHeaders) {
        this.mandatorySignedHeaders = mandatorySignedHeaders;
    }

    public List<List<String>> getKeyDb() {
        return keyDb;
    }

    public Map<String,String> getKeyDbAsMap() {
        return keyDb.stream().collect(Collectors.toMap(p -> p.get(0), p -> p.get(1)));
    }

    public void setKeyDb(List<List<String>> keyDb) {
        this.keyDb = keyDb;
    }

    public Expected getExpected() {
        return expected;
    }

    public void setExpected(Expected expected) {
        this.expected = expected;
    }

    @Override
    public String toString() {
        return getTitle() + ": " + getRequest().getUrl();
    }
}
