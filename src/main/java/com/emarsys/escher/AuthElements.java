package com.emarsys.escher;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AuthElements {

    private String hashAlgo;
    private String accessKeyId;
    private String credentialDate;
    private String credentialScope;
    private List<String> signedHeaders = new ArrayList<>();
    private String signature;
    private boolean fromHeaders;
    private int expires;


    public static AuthElements parseHeader(String text, Config config) throws EscherException {

        Pattern pattern = Pattern.compile("^" + config.getAlgoPrefix() + "-HMAC-(?<hashAlgo>[A-Z0-9,]+) Credential=(?<accessKeyId>[\\w\\-]+)/(?<date>\\d{8})/(?<credentialScope>[\\w\\-/]+), SignedHeaders=(?<signedHeaders>[A-Za-z\\-;]+), Signature=(?<signature>[0-9a-f]+)$");
        Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            AuthElements elements = new AuthElements();

            elements.hashAlgo = matcher.group("hashAlgo");
            elements.accessKeyId = matcher.group("accessKeyId");
            elements.credentialDate = matcher.group("date");
            elements.credentialScope = matcher.group("credentialScope");
            elements.signedHeaders.addAll(Arrays.asList(matcher.group("signedHeaders").split(";")));
            elements.signature = matcher.group("signature");
            elements.fromHeaders = true;
            elements.expires = 0;

            return elements;
        } else {
            throw new EscherException("Malformed authorization header");
        }

    }


    public static AuthElements parseQuery(URI uri, Config config) throws EscherException {
        Map<String, String> parameters = findQueryParameters(uri);

        AuthElements elements = new AuthElements();

        elements.signature = getParam(config, parameters, "Signature");
        parseAlgorithm(elements, getParam(config, parameters, "Algorithm"), config.getAlgoPrefix());
        parseCredentials(elements, getParam(config, parameters, "Credentials"));
        elements.signedHeaders.add("host");
        elements.fromHeaders = false;
        elements.expires = Integer.parseInt(getParam(config, parameters, "Expires"));

        return elements;
    }


    private static Map<String, String> findQueryParameters(URI uri) {
        URIBuilder uriBuilder = new URIBuilder(uri);
        Map<String, String> parameters = new HashMap<>();
        uriBuilder.getQueryParams().forEach(nameValuePair -> parameters.put(nameValuePair.getName(), nameValuePair.getValue()));
        return parameters;
    }


    private static String getParam(Config config, Map<String, String> parameters, String paramName) throws EscherException {
        String fullParamName = "X-" + config.getVendorKey() + "-" + paramName;
        String paramValue = parameters.get(fullParamName);
        if (paramValue == null) {
            throw new EscherException("Missing authorization parameter: " + fullParamName);
        }
        return paramValue;
    }


    private static void parseAlgorithm(AuthElements elements, String algorithm, String algoPrefix) throws EscherException {
        Pattern pattern = Pattern.compile("^" + algoPrefix + "-HMAC-(?<hashAlgo>[A-Z0-9,]+)$");
        Matcher matcher = pattern.matcher(algorithm);
        if (!matcher.matches()) {
            throw new EscherException("Malformed Algorithm parameter");
        }
        elements.hashAlgo = matcher.group("hashAlgo");
    }


    private static void parseCredentials(AuthElements elements, String credentials) throws EscherException {
        Matcher matcher = Pattern.compile("(?<accessKeyId>[\\w\\-]+)/(?<date>\\d{8})/(?<credentialScope>[\\w\\-/]+)").matcher(credentials);
        if (!matcher.matches()) {
            throw new EscherException("Malformed Credentials parameter");
        }
        elements.accessKeyId = matcher.group("accessKeyId");
        elements.credentialDate = matcher.group("date");
        elements.credentialScope = matcher.group("credentialScope");
    }


    public String getHashAlgo() {
        return hashAlgo;
    }


    public String getAccessKeyId() {
        return accessKeyId;
    }


    public String getCredentialDate() {
        return credentialDate;
    }


    public String getCredentialScope() {
        return credentialScope;
    }


    public List<String> getSignedHeaders() {
        return signedHeaders;
    }


    public String getSignature() {
        return signature;
    }


    public boolean isFromHeaders() {
        return fromHeaders;
    }


    public int getExpires() {
        return expires;
    }
}
