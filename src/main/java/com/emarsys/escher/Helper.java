package com.emarsys.escher;

import com.emarsys.escher.util.DateTime;
import com.emarsys.escher.util.Hmac;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;


class Helper {

    private static final char NEW_LINE = '\n';
    private static final String CHARSET = "UTF-8";

    private final Config config;


    public Helper (Config config) {
        this.config = config;
    }


    public String canonicalize(EscherRequest request, List<String> signedHeaders) throws EscherException {
        return request.getHttpMethod() + NEW_LINE +
                canonicalizePath(request) + NEW_LINE +
                canonicalizeQueryParameters(request) + NEW_LINE +
                canonicalizeHeaders(request.getRequestHeaders(), signedHeaders) + NEW_LINE +
                NEW_LINE +
                signedHeaders(signedHeaders) + NEW_LINE +
                Hmac.hash(request.getBody());
    }


    private String canonicalizePath(EscherRequest request) throws EscherException {
        try {
            String path = request.getURI().toURL().getPath();
            return path.equals("") ? "/" : path;
        } catch (MalformedURLException e) {
            throw new EscherException(e);
        }
    }


    private String canonicalizeQueryParameters(EscherRequest request) {
        return URLEncodedUtils.parse(request.getURI(), CHARSET)
                .stream()
                .filter(entry -> !entry.getName().equals("X-" + config.getVendorKey() + "-Signature"))
                .map(this::queryParameterToString)
                .sorted()
                .reduce(byJoiningWith('&'))
                .orElse("");
    }


    private String queryParameterToString(NameValuePair entry) {
        try {
            return URLEncoder.encode(entry.getName(), CHARSET) + "=" + URLEncoder.encode(entry.getValue(), CHARSET);
        } catch (UnsupportedEncodingException shouldNeverHappen) {
            throw new RuntimeException(shouldNeverHappen);
        }
    }


    private String canonicalizeHeaders(List<EscherRequest.Header> headers, List<String> signedHeaders) {
        return headers
                .stream()
                .filter(shouldHeaderBeSigned(signedHeaders))
                .map(header -> header.getFieldName().toLowerCase() + ":" + header.getFieldValue().trim())
                .sorted()
                .reduce(byJoiningWith(NEW_LINE))
                .orElse("");
    }


    private Predicate<EscherRequest.Header> shouldHeaderBeSigned(List<String> signedHeaders) {
        return header -> signedHeaders
                .stream()
                .anyMatch(signedHeader -> signedHeader.equalsIgnoreCase(header.getFieldName()));
    }


    private String signedHeaders(List<String> headers) {
        return headers
                .stream()
                .map(String::toLowerCase)
                .sorted()
                .reduce(byJoiningWith(';'))
                .orElse("");
    }


    private BinaryOperator<String> byJoiningWith(char separator) {
        return (s1, s2) -> s1 + separator + s2;
    }


    public String calculateStringToSign(Instant date, String credentialScope, String canonicalizedRequest) throws EscherException {
        return config.getFullAlgorithm() + NEW_LINE
                + DateTime.toLongString(date) + NEW_LINE
                + DateTime.toShortString(date) + "/" + credentialScope + NEW_LINE
                + Hmac.hash(canonicalizedRequest);
    }


    public byte[] calculateSigningKey(String secret, Instant date, String credentialScope) throws EscherException {
        byte[] key = Hmac.sign(config.getHashAlgo(), (config.getAlgoPrefix() + secret), DateTime.toShortString(date));

        for (String credentialPart : credentialScope.split("/")) {
            key = Hmac.sign(config.getHashAlgo(), key, credentialPart);
        }

        return key;
    }


    public String calculateAuthHeader(String accessKeyId, Instant date, String credentialScope, List<String> signedHeaders, String signature) {
        return config.getFullAlgorithm() +
                " Credential=" + credentials(accessKeyId, date, credentialScope) +
                ", SignedHeaders=" + signedHeaders.stream().reduce((s1, s2) -> s1 + ";" + s2).get().toLowerCase() +
                ", Signature=" + signature;
    }


    public String calculateSignature(byte[] signingKey, String stringToSign) throws EscherException {
        return DatatypeConverter.printHexBinary(Hmac.sign(config.getHashAlgo(), signingKey, stringToSign)).toLowerCase();
    }


    private String credentials(String accessKeyId, Instant date, String credentialScope) {
        return accessKeyId + "/" + DateTime.toShortString(date) + "/" + credentialScope;
    }


    public Map<String, String> calculateSigningParams(String accessKeyId, Instant date, String credentialScope, int expires) {
        Map<String, String> params = new TreeMap<>();
        params.put("SignedHeaders", "host");
        params.put("Expires", Integer.toString(expires));
        params.put("Algorithm", config.getFullAlgorithm());
        params.put("Credentials", credentials(accessKeyId, date, credentialScope));
        params.put("Date", DateTime.toLongString(date));
        return params;
    }


    public void addMandatoryHeaders(EscherRequest request, Instant date) {
        boolean requestHasDateHeader = request.getRequestHeaders()
                .stream()
                .anyMatch(header -> header.getFieldName().equals(config.getDateHeaderName()));
        if (!requestHasDateHeader) {
            String formattedDate = DateTime.toLongString(date);
            request.addHeader(config.getDateHeaderName(), formattedDate);
            Logger.log("Header added - " + config.getDateHeaderName() + ": " + formattedDate);
        }

        boolean requestHasHostHeader = request.getRequestHeaders()
                .stream()
                .anyMatch(header -> header.getFieldName().equalsIgnoreCase("host"));
        if (!requestHasHostHeader) {
            String host = calculateHost(request.getURI());
            request.addHeader("host", host);
            Logger.log("Header added - host: " + host);
        }

    }


    private String calculateHost(URI uri) {
        String host = uri.getHost();
        int port = uri.getPort();
        int defaultPort = ("https".equals(uri.getScheme()) ? 443 : 80);
        if (port != -1 && port != defaultPort) {
            host += ":" + port;
        }
        return host;
    }


    public void addMandatorySignedHeaders(List<String> signedHeaders) {
        boolean asDateHeader = signedHeaders
                .stream()
                .anyMatch(header -> header.equals(config.getDateHeaderName()));
        if (!asDateHeader) {
            signedHeaders.add(config.getDateHeaderName());
        }

        boolean hasHostHeader = signedHeaders
                .stream()
                .anyMatch(header -> header.equalsIgnoreCase("host"));
        if (!hasHostHeader) {
            signedHeaders.add("host");
        }

        Logger.log("Headers to sign: " + signedHeaders.stream().reduce((s1, s2) -> s1 + ", " + s2).orElse(""));
    }


    public void addAuthHeader(EscherRequest request, String fieldValue) {
        request.getRequestHeaders().removeIf(header -> header.getFieldName().equals(config.getAuthHeaderName()));
        request.addHeader(config.getAuthHeaderName(), fieldValue);

        Logger.log("Auth header added - " + config.getAuthHeaderName() + ": " + fieldValue);
    }


    public String parseHostHeader(EscherRequest request) throws EscherException {
        try {
            return findHeader(request, "host").getFieldValue();
        } catch (NoSuchElementException e) {
            throw new EscherException("Missing header: host");
        }
    }


    public AuthElements parseAuthElements(EscherRequest request) throws EscherException {
        if (hasAuthHeader(request)) {
            return AuthElements.parseHeader(findHeader(request, config.getAuthHeaderName()).getFieldValue(), config);
        } else if (hasSignatureQueryParam(request.getURI())) {
            return AuthElements.parseQuery(request.getURI(), config);
        }
        throw new EscherException("Request has not been signed.");
    }


    public Instant parseDate(EscherRequest request) throws EscherException {
        String date;
        if (hasAuthHeader(request)) {
            try {
                date = findHeader(request, config.getDateHeaderName()).getFieldValue();
            } catch (NoSuchElementException e) {
                throw new EscherException("Missing header: " + config.getDateHeaderName());
            }
        } else {
            String dateParamName = "X-" + config.getVendorKey() + "-Date";
            try {
                date = new URIBuilder(request.getURI()).getQueryParams()
                        .stream()
                        .filter(nameValuePair -> nameValuePair.getName().equals(dateParamName))
                        .map(NameValuePair::getValue)
                        .findFirst()
                        .get();
            } catch (NoSuchElementException e) {
                throw new EscherException("Missing authorization parameter: " + dateParamName);
            }
        }
        return DateTime.parseLongString(date);
    }


    private boolean hasAuthHeader(EscherRequest request) {
        try {
            findHeader(request, config.getAuthHeaderName());
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }


    private boolean hasSignatureQueryParam(URI uri) {
        String paramName = "X-" + config.getVendorKey() + "-Signature";
        URIBuilder uriBuilder = new URIBuilder(uri);
        return uriBuilder.getQueryParams()
                .stream()
                .anyMatch(nameValuePair -> nameValuePair.getName().equals(paramName));
    }


    private EscherRequest.Header findHeader(EscherRequest request, String headerName) throws NoSuchElementException {
        return request.getRequestHeaders()
                .stream()
                .filter(header -> header.getFieldName().replace('_', '-').equalsIgnoreCase(headerName))
                .findFirst()
                .get();
    }

}
