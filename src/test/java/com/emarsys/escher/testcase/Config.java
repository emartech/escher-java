package com.emarsys.escher.testcase;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public class Config {
    private String vendorKey;
    private String algoPrefix;
    private String hashAlgo;
    private String clockSkew;
    private String credentialScope;
    private String accessKeyId;
    private String apiSecret;
    private String authHeaderName;
    private String dateHeaderName;
    private String date;

    public String getVendorKey() {
        return vendorKey;
    }

    public void setVendorKey(String vendorKey) {
        this.vendorKey = vendorKey;
    }

    public String getAlgoPrefix() {
        return algoPrefix;
    }

    public void setAlgoPrefix(String algoPrefix) {
        this.algoPrefix = algoPrefix;
    }

    public String getHashAlgo() {
        return hashAlgo;
    }

    public void setHashAlgo(String hashAlgo) {
        this.hashAlgo = hashAlgo;
    }

    public String getCredentialScope() {
        return credentialScope;
    }

    public void setCredentialScope(String credentialScope) {
        this.credentialScope = credentialScope;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAuthHeaderName() {
        return authHeaderName;
    }

    public void setAuthHeaderName(String authHeaderName) {
        this.authHeaderName = authHeaderName;
    }

    public String getDateHeaderName() {
        return dateHeaderName;
    }

    public void setDateHeaderName(String dateHeaderName) {
        this.dateHeaderName = dateHeaderName;
    }

    public String getDate() {
        return date;
    }

    public Instant getDateAsInstant() {
        try {
            return Instant.from(ISO_INSTANT.parse(date));
        } catch (DateTimeParseException e) {
            return Instant.from(RFC_1123_DATE_TIME.parse(date));
        }
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getClockSkew() {
        return clockSkew;
    }

    public void setClockSkew(String clockSkew) {
        this.clockSkew = clockSkew;
    }
}
