package com.emarsys.escher;

import org.apache.http.client.utils.URLEncodedUtils;

import javax.xml.bind.DatatypeConverter;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.BinaryOperator;

class Helper {

    private static final char NEW_LINE = '\n';

    private final Config config;


    public Helper (Config config) {
        this.config = config;
    }


    public String canonicalize(Request request) throws EscherException {
        try {
            return request.getHttpMethod() + NEW_LINE +
                    request.getURI().toURL().getPath() + NEW_LINE +
                    canonicalizeQueryParameters(request) + NEW_LINE +
                    canonicalizeHeaders(request.getRequestHeaders()) + NEW_LINE +
                    NEW_LINE +
                    signedHeaders(request.getRequestHeaders()) + NEW_LINE +
                    Hmac.hash(request.getBody());
        } catch (MalformedURLException e) {
            throw new EscherException(e);
        }
    }


    private String canonicalizeQueryParameters(Request request) {
        return URLEncodedUtils.parse(request.getURI(), "utf-8")
                .stream()
                .map(entry -> entry.getName() + "=" + URLEncoder.encode(entry.getValue()))
                .sorted()
                .reduce(byJoiningWith('&'))
                .orElseGet(() -> "");
    }


    private String canonicalizeHeaders(List<Request.Header> headers) {
        return headers
                .stream()
                .map(header -> header.getFieldName().toLowerCase() + ":" + header.getFieldValue().trim())
                .sorted()
                .reduce(byJoiningWith(NEW_LINE))
                .orElseGet(() -> "");
    }


    private String signedHeaders(List<Request.Header> headers) {
        return headers
                .stream()
                .map(header -> header.getFieldName().toLowerCase())
                .sorted()
                .reduce(byJoiningWith(';'))
                .orElseGet(() -> "");
    }


    private BinaryOperator<String> byJoiningWith(char separator) {
        return (s1, s2) -> s1 + separator + s2;
    }


    public String calculateStringToSign(String credentialScope, String canonicalizedRequest) throws EscherException {
        return config.getFullAlgorithm() + NEW_LINE
                + longDate() + NEW_LINE
                + shortDate() + "/" + credentialScope + NEW_LINE
                + Hmac.hash(canonicalizedRequest);
    }


    public byte[] calculateSigningKey(String secret, String credentialScope) throws EscherException {
        byte[] key = Hmac.sign(config.getHashAlgo(), (config.getAlgoPrefix() + secret), shortDate());

        for (String credentialPart : credentialScope.split("/")) {
            key = Hmac.sign(config.getHashAlgo(), key, credentialPart);
        }

        return key;
    }


    private String longDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(config.getCurrentTime());
    }


    private String shortDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(config.getCurrentTime());
    }


    public String calculateAuthHeader(String accessKeyId, String credentialScope, List<String> signedHeaders, String signature) {
        return config.getFullAlgorithm() +
                " Credential=" + credentials(accessKeyId, credentialScope) +
                ", SignedHeaders=" + signedHeaders.stream().reduce((s1, s2) -> s1 + ";" + s2).get().toLowerCase() +
                ", Signature=" + signature;
    }


    public String calculateSignature(byte[] signingKey, String stringToSign) throws EscherException {
        return DatatypeConverter.printHexBinary(Hmac.sign(config.getHashAlgo(), signingKey, stringToSign)).toLowerCase();
    }


    private String credentials(String accessKeyId, String credentialScope) {
        return accessKeyId + "/" + shortDate() + "/" + credentialScope;
    }


    public Map<String, String> calculateSigningParams(String accessKeyId, String credentialScope, int expires) {
        Map<String, String> params = new TreeMap<>();
        params.put("SignedHeaders", "host");
        params.put("Expires", Integer.toString(expires));
        params.put("Algorithm", config.getFullAlgorithm());
        params.put("Credentials", credentials(accessKeyId, credentialScope));
        params.put("Date", longDate());

        return params;
    }


    public void addDateHeader(Request request) {
        if (!request.hasHeader(config.getDateHeaderName())) {
            request.addHeader(config.getDateHeaderName(), longDate());
        }
    }


    public void addAuthHeader(Request request, String header) {
        request.addHeader(config.getAuthHeaderName(), header);
    }

}
