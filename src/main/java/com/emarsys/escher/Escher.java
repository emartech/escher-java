package com.emarsys.escher;


import com.emarsys.escher.util.DateTime;
import org.apache.http.client.utils.URIBuilder;

import javax.xml.bind.DatatypeConverter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Escher {

    public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";

    private String credentialScope;
    private String algoPrefix = "ESR";
    private String vendorKey = "Escher";
    private String hashAlgo = "SHA256";
    private Date currentTime = new Date();
    private String authHeaderName = "X-Escher-Auth";
    private String dateHeaderName = "X-Escher-Date";
    private int clockSkew = 900;

    public Escher(String credentialScope) {
        this.credentialScope = credentialScope;
    }


    public EscherRequest signRequest(EscherRequest request, String accessKeyId, String secret, List<String> signedHeaders) throws EscherException {
        Config config = createConfig();
        Helper helper = new Helper(config);

        helper.addMandatoryHeaders(request, currentTime);
        helper.addMandatorySignedHeaders(signedHeaders);

        String signature = calculateSignature(helper, request, secret, signedHeaders, currentTime);
        String authHeader = helper.calculateAuthHeader(accessKeyId, currentTime, credentialScope, signedHeaders, signature);

        helper.addAuthHeader(request, authHeader);

        return request;
    }


    public String presignUrl(String url, String accessKeyId, String secret, int expires) throws EscherException{
        try {
            Config config = createConfig();
            Helper helper = new Helper(config);

            URI uri = new URI(url);
            URIBuilder uriBuilder = new URIBuilder(uri);

            Map<String, String> params = helper.calculateSigningParams(accessKeyId, currentTime, credentialScope, expires);
            params.forEach((key, value) -> uriBuilder.addParameter("X-" + vendorKey + "-" + key, value));

            EscherRequest request = new PresignUrlDummyEscherRequest(uriBuilder.build());

            String signature = calculateSignature(helper, request, secret, Arrays.asList("host"), currentTime);

            uriBuilder.addParameter("X-" + vendorKey + "-" + "Signature", signature);

            return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new EscherException(e);
        }
    }


    public String authenticate(EscherRequest request, Map<String, String> keyDb, InetSocketAddress address) throws EscherException {
        Config config = createConfig();
        Helper helper = new Helper(config);

        AuthElements authElements = helper.parseAuthHeader(request);
        Date requestDate = helper.parseDateHeader(request);
        String hostHeader = helper.parseHostHeader(request);

        AuthenticationValidator validator = new AuthenticationValidator(config);

        validator.validateMandatorySignedHeaders(authElements.getSignedHeaders());
        validator.validateHashAlgo(authElements.getHashAlgo());
        validator.validateDates(requestDate, DateTime.parseShortString(authElements.getCredentialDate()), currentTime);
        validator.validateHost(address, hostHeader);
        validator.validateCredentialScope(credentialScope, authElements.getCredentialScope());

        String secret = retrieveSecret(keyDb, authElements.getAccessKeyId());
        String calculatedSignature = calculateSignature(helper, request, secret, authElements.getSignedHeaders(), requestDate);

        validator.validateSignature(calculatedSignature, authElements.getSignature());

        return authElements.getAccessKeyId();
    }


    private String retrieveSecret(Map<String, String> keyDb, String accessKeyId) throws EscherException {
        String secret = keyDb.get(accessKeyId);

        if (secret == null) {
            throw new EscherException("Invalid access key id");
        }
        return secret;
    }


    private String calculateSignature(Helper helper, EscherRequest request, String secret, List<String> signedHeaders, Date date) throws EscherException {
        String canonicalizedRequest = helper.canonicalize(request, signedHeaders);
        String stringToSign = helper.calculateStringToSign(date, credentialScope, canonicalizedRequest);
        byte[] signingKey = helper.calculateSigningKey(secret, date, credentialScope);
        String signature = helper.calculateSignature(signingKey, stringToSign);

        Logger.log("Canonicalized request: " + canonicalizedRequest);
        Logger.log("String to sign: " + stringToSign);
        Logger.log("Signing key: " + DatatypeConverter.printHexBinary(signingKey));
        Logger.log("Signature: " + signature);

        return signature;
    }


    private Config createConfig() {
        return Config.create()
                .setAlgoPrefix(algoPrefix)
                .setHashAlgo(hashAlgo)
                .setDateHeaderName(dateHeaderName)
                .setAuthHeaderName(authHeaderName)
                .setClockSkew(clockSkew);
    }


    public Escher setAlgoPrefix(String algoPrefix) {
        this.algoPrefix = algoPrefix;
        return this;
    }


    public Escher setVendorKey(String vendorKey) {
        this.vendorKey = vendorKey;
        return this;
    }


    public Escher setHashAlgo(String hashAlgo) {
        this.hashAlgo = hashAlgo;
        return this;
    }


    public Escher setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
        return this;
    }


    public Escher setAuthHeaderName(String authHeaderName) {
        this.authHeaderName = authHeaderName;
        return this;
    }


    public Escher setDateHeaderName(String dateHeaderName) {
        this.dateHeaderName = dateHeaderName;
        return this;
    }


    public Escher setClockSkew(int clockSkew) {
        this.clockSkew = clockSkew;
        return this;
    }


    public Escher setLogger(Consumer<String> logger) {
        if (logger == null) {
            throw new IllegalArgumentException("Logger is null");
        }
        Logger.setConsumer(logger);
        return this;
    }
}
