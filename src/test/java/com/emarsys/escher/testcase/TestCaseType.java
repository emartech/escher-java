package com.emarsys.escher.testcase;

public enum TestCaseType {
    SIGN_REQUEST("signrequest"),
    AUTHENTICATE("authenticate"),
    PRESIGN_URL("presignurl");

    private final String prefix;

    TestCaseType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
