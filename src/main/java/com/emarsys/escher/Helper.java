package com.emarsys.escher;

import org.apache.http.NameValuePair;

import javax.xml.bind.DatatypeConverter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.BinaryOperator;

class Helper {

    private static final char NEW_LINE = '\n';


    public static String canonicalize(Request request) throws EscherException {
        return request.getHttpMethod() + NEW_LINE +
                request.getPath() + NEW_LINE +
                canonicalizeQueryParameters(request) + NEW_LINE +
                canonicalizeHeaders(request.getHeaders()) + NEW_LINE +
                NEW_LINE +
                signedHeaders(request.getHeaders()) + NEW_LINE +
                Hmac.hash(request.getBody());
    }


    private static String canonicalizeQueryParameters(Request request) {
        return request.getQueryParameters()
                .stream()
                .map(entry -> entry.getName() + "=" + entry.getValue())
                .sorted()
                .reduce(byJoiningWith('&'))
                .orElseGet(() -> "");
    }


    private static String canonicalizeHeaders(List<NameValuePair> headers) {
        return headers
                .stream()
                .map(nameValuePair -> nameValuePair.getName().toLowerCase() + ":" + nameValuePair.getValue().trim())
                .sorted()
                .reduce(byJoiningWith(NEW_LINE))
                .get();
    }


    private static String signedHeaders(List<NameValuePair> headers) {
        return headers
                .stream()
                .map(nameValuePair -> nameValuePair.getName().toLowerCase())
                .sorted()
                .reduce(byJoiningWith(';'))
                .get();
    }


    private static BinaryOperator<String> byJoiningWith(char separator) {
        return (s1, s2) -> s1 + separator + s2;
    }


    public static String calculateStringToSign(String credentialScope, String canonicalizedRequest, Date date, String hashAlgo, String algoPrefix) throws EscherException{
        return algoPrefix + "-HMAC-" + hashAlgo + NEW_LINE
                + longDate(date) + NEW_LINE
                + shortDate(date) + "/" + credentialScope + NEW_LINE
                + Hmac.hash(canonicalizedRequest);
    }


    public static byte[] calculateSigningKey(String secret, Date date, String credentialScope, String hashAlgo, String algoPrefix) throws EscherException{
        byte[] key = Hmac.sign(hashAlgo, (algoPrefix + secret), shortDate(date));

        for (String credentialPart : credentialScope.split("/")) {
            key = Hmac.sign(hashAlgo, key, credentialPart);
        }

        return key;
    }


    private static String longDate(Date date) {
        return new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'").format(date);
    }


    private static String shortDate(Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }


    public static String calculateAuthHeader(String accessKeyId, Date date, String credentialScope, byte[] signingKey, String hashAlgo, String algoPrefix, List<String> signedHeaders, String stringToSign) throws EscherException {
        String signature = DatatypeConverter.printHexBinary(Hmac.sign(hashAlgo, signingKey, stringToSign)).toLowerCase();
        return algoPrefix + "-HMAC-" + hashAlgo +
                " Credential=" + accessKeyId + "/" + shortDate(date) + "/" + credentialScope +
                ", SignedHeaders=" + signedHeaders.stream().reduce((s1, s2) -> s1 + ";" + s2).get().toLowerCase() +
                ", Signature=" + signature;
    }
}
