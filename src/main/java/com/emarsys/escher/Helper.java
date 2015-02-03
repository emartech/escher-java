package com.emarsys.escher;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class Helper {

    private static final char NEW_LINE = '\n';

    static String canonicalize(Request request) throws EscherException {
        return request.getHttpMethod() + NEW_LINE +
                request.getPath() + NEW_LINE +
                "" + NEW_LINE +
                canonicalizeHeaders(request.getHeaders()) + NEW_LINE +
                NEW_LINE +
                signedHeaders(request.getHeaders()) + NEW_LINE +
                hash(request.getBody());
    }

    private static String canonicalizeHeaders(List<String[]> headers) {
        return headers
                .stream()
                .map(array -> array[0].toLowerCase() + ":" + array[1])
                .sorted()
                .reduce((s1, s2) -> s1 + NEW_LINE + s2)
                .get();
    }

    private static String signedHeaders(List<String[]> headers) {
        return headers
                .stream()
                .map(array -> array[0].toLowerCase())
                .sorted()
                .reduce((s1, s2) -> s1 + ';' + s2)
                .get();
    }

    private static String hash(String text) throws EscherException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes("utf-8"));
            byte[] bytes = md.digest();
            return DatatypeConverter.printHexBinary(bytes).toLowerCase();
        } catch (Exception e) {
            throw new EscherException("Unable to compute hash", e);
        }
    }


    public static String calculateStringToSign(String credentialScope, String canonicalizedRequest, Date date, String hashAlgo, String algoPrefix) throws EscherException{
        return algoPrefix + "-HMAC-" + hashAlgo + NEW_LINE
                + new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'").format(date) + NEW_LINE
                + new SimpleDateFormat("yyyyMMdd").format(date) + "/" + credentialScope + NEW_LINE
                + hash(canonicalizedRequest);
    }
}
