package com.emarsys.escher;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.List;

class Helper {

    private static final char NEW_LINE = '\n';

    static String canonicalize(Request request) throws EscherException {

        String canonicalizedHeaders = canonicalizeHeaders(request.getHeaders());
        String signedHeaders = signedHeaders(request.getHeaders());
        String bodyHash = bodyHash(request.getBody());

        return request.getHttpMethod() + NEW_LINE +
                request.getPath() + NEW_LINE +
                "" + NEW_LINE +
                canonicalizedHeaders + NEW_LINE +
                NEW_LINE +
                signedHeaders + NEW_LINE +
                bodyHash;
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

    private static String bodyHash(String body) throws EscherException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(body.getBytes("utf-8"));
            byte[] bytes = md.digest();
            return DatatypeConverter.printHexBinary(bytes).toLowerCase();
        } catch (Exception e) {
            throw new EscherException("Unable to compute hash", e);
        }
    }

}
