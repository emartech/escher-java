package com.emarsys.escher;

import org.apache.http.client.utils.URLEncodedUtils;

import javax.xml.bind.DatatypeConverter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BinaryOperator;

class Helper {

    private static final char NEW_LINE = '\n';

    private Config config;


    public Helper (Config config) {
        this.config = config;
    }


    public String canonicalize(Request request) throws EscherException {
        return request.getHttpMethod() + NEW_LINE +
                request.getURI().getPath() + NEW_LINE +
                canonicalizeQueryParameters(request) + NEW_LINE +
                canonicalizeHeaders(request.getRequestHeaders()) + NEW_LINE +
                NEW_LINE +
                signedHeaders(request.getRequestHeaders()) + NEW_LINE +
                Hmac.hash(request.getBody());
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


    public String calculateStringToSign(String credentialScope, String canonicalizedRequest, Date date) throws EscherException{
        return algorithm(config.getAlgoPrefix(), config.getHashAlgo()) + NEW_LINE
                + longDate(date) + NEW_LINE
                + shortDate(date) + "/" + credentialScope + NEW_LINE
                + Hmac.hash(canonicalizedRequest);
    }


    public byte[] calculateSigningKey(String secret, Date date, String credentialScope) throws EscherException{
        byte[] key = Hmac.sign(config.getHashAlgo(), (config.getAlgoPrefix() + secret), shortDate(date));

        for (String credentialPart : credentialScope.split("/")) {
            key = Hmac.sign(config.getHashAlgo(), key, credentialPart);
        }

        return key;
    }


    public String longDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }


    private String shortDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }


    public String calculateAuthHeader(String accessKeyId, Date date, String credentialScope, List<String> signedHeaders, String signature) {
        return algorithm(config.getAlgoPrefix(), config.getHashAlgo()) +
                " Credential=" + credentials(accessKeyId, date, credentialScope) +
                ", SignedHeaders=" + signedHeaders.stream().reduce((s1, s2) -> s1 + ";" + s2).get().toLowerCase() +
                ", Signature=" + signature;
    }


    public String calculateSignature(byte[] signingKey, String stringToSign) throws EscherException {
        return DatatypeConverter.printHexBinary(Hmac.sign(config.getHashAlgo(), signingKey, stringToSign)).toLowerCase();
    }


    private String credentials(String accessKeyId, Date date, String credentialScope) {
        return accessKeyId + "/" + shortDate(date) + "/" + credentialScope;
    }


    public Map<String, String> calculateSigningParams(String accessKeyId, Date date, String credentialScope, int expires) {
        Map<String, String> params = new TreeMap<>();
        params.put("SignedHeaders", "host");
        params.put("Expires", Integer.toString(expires));
        params.put("Algorithm", algorithm(config.getAlgoPrefix(), config.getHashAlgo()));
        params.put("Credentials", credentials(accessKeyId, date, credentialScope));
        params.put("Date", longDate(date));

        return params;
    }


    private String algorithm(String algoPrefix, String hashAlgo) {
        return algoPrefix + "-HMAC-" + hashAlgo;
    }

}
