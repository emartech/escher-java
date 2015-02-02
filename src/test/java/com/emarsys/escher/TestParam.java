package com.emarsys.escher;


import java.util.List;

public class TestParam {

    public static class Request {

        private String host;
        private String method;
        private String url;
        private List<List<String>> headers;
        private String body;


        public String getHost() {
            return host;
        }


        public void setHost(String host) {
            this.host = host;
        }


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


        public void setHeaders(List<List<String>> headers) {
            this.headers = headers;
        }


        public String getBody() {
            return body;
        }


        public void setBody(String body) {
            this.body = body;
        }
    }

    public static class Config {

        private String vendorKey;
        private String algoPrefix;
        private String hashAlgo;
        private String credentialScope;
        private String apiSecret;
        private String accessKeyId;
        private String authHeaderName;
        private String dateHeaderName;
        private String date;


        public String getVendorKey() {
            return vendorKey;
        }


        public void setVendorKey(String vendorKey) {
            this.vendorKey = vendorKey;
        }


        public String getAlgoPrefix() {
            return algoPrefix;
        }


        public void setAlgoPrefix(String algoPrefix) {
            this.algoPrefix = algoPrefix;
        }


        public String getHashAlgo() {
            return hashAlgo;
        }


        public void setHashAlgo(String hashAlgo) {
            this.hashAlgo = hashAlgo;
        }


        public String getCredentialScope() {
            return credentialScope;
        }


        public void setCredentialScope(String credentialScope) {
            this.credentialScope = credentialScope;
        }


        public String getApiSecret() {
            return apiSecret;
        }


        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }


        public String getAccessKeyId() {
            return accessKeyId;
        }


        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }


        public String getAuthHeaderName() {
            return authHeaderName;
        }


        public void setAuthHeaderName(String authHeaderName) {
            this.authHeaderName = authHeaderName;
        }


        public String getDateHeaderName() {
            return dateHeaderName;
        }


        public void setDateHeaderName(String dateHeaderName) {
            this.dateHeaderName = dateHeaderName;
        }


        public String getDate() {
            return date;
        }


        public void setDate(String date) {
            this.date = date;
        }
    }

    public static class Expected {
        private Request request;
        private String canonicalizedRequest;
        private String stringToSign;
        private String authHeader;


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
    }

    private String date;
    private List<String> headersToSign;
    private Request request;
    private Config config;
    private Expected expected;


    public String getDate() {
        return date;
    }


    public void setDate(String date) {
        this.date = date;
    }


    public List<String> getHeadersToSign() {
        return headersToSign;
    }


    public void setHeadersToSign(List<String> headersToSign) {
        this.headersToSign = headersToSign;
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


    public Expected getExpected() {
        return expected;
    }


    public void setExpected(Expected expected) {
        this.expected = expected;
    }
}
