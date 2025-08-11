package com.emarsys.escher;

import com.emarsys.escher.util.DateTime;
import com.emarsys.escher.util.Hmac;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;


class Helper {

    private static final char NEW_LINE = '\n';

    private final Config config;


    public Helper(Config config) {
        this.config = config;
    }


    public String canonicalize(EscherRequest request, List<String> signedHeaders) throws EscherException {
        return request.getHttpMethod().toUpperCase() + NEW_LINE +
                canonicalizePath(request) + NEW_LINE +
                canonicalizeQueryParameters(request) + NEW_LINE +
                canonicalizeHeaders(request.getRequestHeaders(), signedHeaders) + NEW_LINE +
                NEW_LINE +
                signedHeaders(signedHeaders) + NEW_LINE +
                Hmac.hash(request.getBody());
    }


    private String canonicalizePath(EscherRequest request) {
        String path = normalizePath(request.getURI().getRawPath());
        return path.isEmpty() ? "/" : path;
    }

    private String normalizePath(String path) {
        String normalizedPath;
        String originalPath = path;
        while (true) {
            normalizedPath = originalPath.replaceFirst("([^/]+/\\.\\./?|/\\./|//|/\\.$|/\\.\\.$)", "/");
            if (normalizedPath.equals(originalPath)) {
                return normalizedPath;
            }
            originalPath = normalizedPath;
        }
    }

    private String canonicalizeQueryParameters(EscherRequest request) {
        String rawQuery = request.getURI().getRawQuery();
        if (rawQuery == null) {
            return "";
        }
        return Arrays.stream(rawQuery.split("&"))
                .filter(pair -> !pair.isEmpty())
                .map(pair -> {
                    String[] keyValue = pair.split("=", 2);
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) : "";
                    return new BasicNameValuePair(key, value);
                })
                .filter(entry -> !entry.getName().equals("X-" + config.getVendorKey() + "-Signature"))
                .map(this::queryParameterToString)
                .sorted()
                .reduce(byJoiningWith('&'))
                .orElse("");
    }


    private String queryParameterToString(NameValuePair entry) {
        try {
            return encodeURIComponent(entry.getName()) + "=" + encodeURIComponent(entry.getValue());
        } catch (UnsupportedEncodingException shouldNeverHappen) {
            throw new RuntimeException(shouldNeverHappen);
        }
    }


    public static String encodeURIComponent(String s) throws UnsupportedEncodingException {
        // We need this to be uri encoded (' ' => '%20') not x-www-form-urlencoded (' ' => '+') with
        // some of the RFC3986 reserved characters kept as they were (-._~) which is used by URLEncoder.
        // This will result an encoding method similar to other escher libs.
        return URLEncoder.encode(Objects.toString(s, ""), "UTF-8")
                .replaceAll("\\+", "%20")
                .replaceAll("\\%2D", "-")
                .replaceAll("\\%2E", ".")
                .replaceAll("\\%5F", "_")
                .replaceAll("\\%7E", "~")
                .replaceAll("\\%21", "!");
    }


    private String canonicalizeHeaders(List<EscherRequest.Header> headers, List<String> signedHeaders) {
        return headers
                .stream()
                .filter(shouldHeaderBeSigned(signedHeaders))
                .collect(Collectors.groupingBy(
                        header -> header.getFieldName().toLowerCase(),
                        Collectors.mapping(
                                header -> normalizeWhiteSpaces(header.getFieldValue().trim()),
                                Collectors.joining(",")
                        )))
                .entrySet()
                .stream()
                .map(header -> header.getKey() + ":" + header.getValue())
                .sorted()
                .reduce(byJoiningWith(NEW_LINE))
                .orElse("");
    }

    private String normalizeWhiteSpaces(String headerValue) {
        AtomicInteger index = new AtomicInteger(0);
        return Arrays.stream(headerValue.trim().split("\"", -1)).map(piece -> {
            boolean isInsideOfQuotes = index.getAndIncrement() % 2 == 1;
            return isInsideOfQuotes ? piece : piece.replaceAll("\\p{javaSpaceChar}{2,}"," ");
        }).collect(Collectors.joining("\""));

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
                ", SignedHeaders=" + this.signedHeaders(signedHeaders) +
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
                .anyMatch(header -> header.getFieldName().equalsIgnoreCase(config.getDateHeaderName()));
        if (!requestHasDateHeader) {
            String formattedDate = DateTime.toHeaderString(date);
            request.addHeader(config.getDateHeaderName(), formattedDate);
        }

        boolean requestHasHostHeader = request.getRequestHeaders()
                .stream()
                .anyMatch(header -> header.getFieldName().equalsIgnoreCase("host"));
        if (!requestHasHostHeader) {
            String host = calculateHost(request.getURI());
            request.addHeader("host", host);
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
                .anyMatch(header -> header.equalsIgnoreCase(config.getDateHeaderName()));
        if (!asDateHeader) {
            signedHeaders.add(config.getDateHeaderName());
        }

        boolean hasHostHeader = signedHeaders
                .stream()
                .anyMatch(header -> header.equalsIgnoreCase("host"));
        if (!hasHostHeader) {
            signedHeaders.add("host");
        }
    }


    public void addAuthHeader(EscherRequest request, String fieldValue) {
        request.getRequestHeaders().removeIf(header -> header.getFieldName().equals(config.getAuthHeaderName()));
        request.addHeader(config.getAuthHeaderName(), fieldValue);
    }


    public String parseHostHeader(EscherRequest request) throws EscherException {
        try {
            return findHeader(request, "host").getFieldValue();
        } catch (NoSuchElementException e) {
            throw new EscherException("The host header is missing");
        }
    }


    public AuthElements parseAuthElements(EscherRequest request) throws EscherException {
        if (hasAuthHeader(request)) {
            return AuthElements.parseHeader(findHeader(request, config.getAuthHeaderName()).getFieldValue(), config);
        } else if (hasSignatureQueryParam(request.getURI())) {
            return AuthElements.parseQuery(request.getURI(), config);
        }
        throw new EscherException("The authorization header is missing");
    }


    public Instant parseDate(EscherRequest request) throws EscherException {
        String date;
        if (hasAuthHeader(request)) {
            try {
                date = findHeader(request, config.getDateHeaderName()).getFieldValue();
            } catch (NoSuchElementException e) {
                throw new EscherException("The date header is missing");
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
        URIBuilder uriBuilder = new URIBuilder(uri);
        return uriBuilder.getQueryParams()
                .stream()
                .anyMatch(nameValuePair -> nameValuePair.getName().equals("X-" + config.getVendorKey() + "-Signature"));
    }


    private EscherRequest.Header findHeader(EscherRequest request, String headerName) throws NoSuchElementException {
        return request.getRequestHeaders()
                .stream()
                .filter(header -> header.getFieldName().replace('_', '-').equalsIgnoreCase(headerName))
                .findFirst()
                .get();
    }

}
