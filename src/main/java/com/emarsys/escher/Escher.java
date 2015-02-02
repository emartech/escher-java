package com.emarsys.escher;


import java.util.Date;

public class Escher {

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

}
