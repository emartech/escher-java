package com.emarsys.escher;

import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.lang.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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

    private static String canonicalizeHeaders(Map<String, String> headers) {
        List<String> resultLines = new ArrayList<>();
        headers.forEach((key, value) -> {
            resultLines.add(key.toLowerCase() + ":" + value);
        });

        Collections.sort(resultLines);

        return StringUtils.join(resultLines, NEW_LINE);
    }

    private static String signedHeaders(Map<String, String> headers) {
        TreeSet<String> headersToSign = new TreeSet<>();
        headers.forEach((key, value) -> {
            headersToSign.add(key.toLowerCase());
        });

        return StringUtils.join(headersToSign, ';');
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
