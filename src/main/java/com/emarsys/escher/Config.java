package com.emarsys.escher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

class Config {

    public static final TimeZone TIMEZONE = TimeZone.getTimeZone("UTC");
    public static final String LONG_DATE_FORMAT = "yyyyMMdd'T'HHmmss'Z'";
    public static final String SHORT_DATE_FORMAT = "yyyyMMdd";


    private String algoPrefix = "ESR";
    private String vendorKey = "Escher";
    private String hashAlgo = "SHA256";
    private String authHeaderName = "X-Escher-Auth";
    private String dateHeaderName = "X-Escher-Date";
    private Date date;
    private int clockSkew = 900;


    private Config() {}


    public static Config create() {
        return new Config();
    }


    public String getAlgoPrefix() {
        return algoPrefix;
    }


    public Config setAlgoPrefix(String algoPrefix) {
        this.algoPrefix = algoPrefix;
        return this;
    }


    public String getVendorKey() {
        return vendorKey;
    }


    public Config setVendorKey(String vendorKey) {
        this.vendorKey = vendorKey;
        return this;
    }


    public String getHashAlgo() {
        return hashAlgo;
    }


    public Config setHashAlgo(String hashAlgo) {
        this.hashAlgo = hashAlgo;
        return this;
    }


    public String getAuthHeaderName() {
        return authHeaderName;
    }


    public Config setAuthHeaderName(String authHeaderName) {
        this.authHeaderName = authHeaderName;
        return this;
    }


    public String getDateHeaderName() {
        return dateHeaderName;
    }


    public Config setDateHeaderName(String dateHeaderName) {
        this.dateHeaderName = dateHeaderName;
        return this;
    }


    public Config setDate(Date date) {
        this.date = date;
        return this;
    }


    public int getClockSkew() {
        return clockSkew;
    }


    public Config setClockSkew(int clockSkew) {
        this.clockSkew = clockSkew;
        return this;
    }


    public String getFullAlgorithm() {
        return algoPrefix + "-HMAC-" + hashAlgo;
    }


    public String getLongFormatDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(LONG_DATE_FORMAT);
        dateFormat.setTimeZone(TIMEZONE);
        return dateFormat.format(date);
    }


    public String getShortFormatDate() {
        SimpleDateFormat format = new SimpleDateFormat(SHORT_DATE_FORMAT);
        format.setTimeZone(TIMEZONE);
        return format.format(date);
    }
}
