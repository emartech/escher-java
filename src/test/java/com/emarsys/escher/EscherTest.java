package com.emarsys.escher;

import com.emarsys.escher.acceptance.StubClock;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(DataProviderRunner.class)
public class EscherTest extends TestBase {


    private StubClock clock = new StubClock();
    private Escher escher;


    @Before
    public void setUp() throws Exception {
        clock.setInstant(createInstant(2011, 9, 9, 23, 40, 0));
        escher = new Escher("us-east-1/iam/aws4_request", clock).setAlgoPrefix("EMS");
    }


    @Test
    public void testPresignUrl() throws Exception {
        clock.setInstant(createInstant(2011, 5, 11, 12, 0, 0));
        Escher escher = new Escher("us-east-1/host/aws4_request", clock)
                .setAlgoPrefix("EMS")
                .setVendorKey("EMS")
                .setAuthHeaderName("X-Ems-Auth")
                .setDateHeaderName("X-Ems-Date");

        int expires = 123456;
        String signedUrl = escher.presignUrl("http://example.com/something?foo=bar&baz=barbaz", "th3K3y", "very_secure", expires);

        String expectedSignedUrl = "http://example.com/something?foo=bar&baz=barbaz&X-EMS-Algorithm=EMS-HMAC-SHA256&X-EMS-Credentials=th3K3y%2F20110511%2Fus-east-1%2Fhost%2Faws4_request&X-EMS-Date=20110511T120000Z&X-EMS-Expires=123456&X-EMS-SignedHeaders=host&X-EMS-Signature=fbc9dbb91670e84d04ad2ae7505f4f52ab3ff9e192b8233feeae57e9022c2b67";
        assertThat(signedUrl, is(expectedSignedUrl));
    }

    @Test
    public void testAuthenticateSuccess() throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X-Ems-Date", "20110909T233600Z"),
                new EscherRequest.Header("X-Ems-Auth", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("Host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com/"), headers, "Action=ListUsers&Version=2010-05-08");

        Map<String, String> keyDb = new HashMap<>();
        keyDb.put("AKIDEXAMPLE", "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY");

        escher.setAlgoPrefix("EMS")
                .setVendorKey("EMS")
                .setAuthHeaderName("X-Ems-Auth")
                .setDateHeaderName("X-Ems-Date");

        String accessKey = escher.authenticate(request, keyDb);

        assertThat(accessKey, is("AKIDEXAMPLE"));
    }


    @Test
    public void testAuthenticatePresignedUrl() throws Exception {
        clock.setInstant(createInstant(2011, 5, 11, 12, 0, 0));
        Escher escher = new Escher("us-east-1/host/aws4_request", clock)
                .setAlgoPrefix("EMS")
                .setVendorKey("EMS")
                .setAuthHeaderName("X-Ems-Auth")
                .setDateHeaderName("X-Ems-Date");

        String key = "accesskey";
        String secret = "very_secure";
        String signedUrl = escher.presignUrl("https://example.com/something?foo=bar&baz=barbaz", key, secret, 123456);

        List<EscherRequest.Header> headers = Collections.singletonList(new EscherRequest.Header("Host", "example.com"));
        EscherRequest request = new EscherRequestImpl("GET", new URI(signedUrl), headers, "");

        Map<String, String> keyDb = new HashMap<>();
        keyDb.put(key, secret);
        String accessKey = escher.authenticate(request, keyDb);

        assertThat(accessKey, is(key));
    }


    @Test
    @UseDataProvider("getAuthenticationMissingHeaderCases")
    public void testAuthenticateMissingHeader(String headerToExlude, String expectedErrorMessage) throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X-Escher-Date", "20110909T233600Z"),
                new EscherRequest.Header("X-Escher-Auth", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-escher-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("Host", "iam.amazonaws.com")
        );

        headers = headers
                .stream()
                .filter(header -> !header.getFieldName().equals(headerToExlude))
                .collect(Collectors.toList());

        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");

        assertAuthenticationError(expectedErrorMessage, request);
    }


    @DataProvider
    public static Object[][] getAuthenticationMissingHeaderCases() {
        return new Object[][] {
                { "Host", "The host header is missing" },
                { "X-Escher-Date", "The date header is missing" },
                { "X-Escher-Auth", "The authorization header is missing" },
        };
    }


    @Test
    public void testAuthenticateInvalidDateFormat() throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X-Escher-Date", "NOT_A_DATE"),
                new EscherRequest.Header("X-Escher-Auth", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-escher-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("Host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");

        assertAuthenticationError("Invalid date format", request);
    }


    @Test
    @UseDataProvider("getAuthenticateMandatoryHeaderNotSignedCases")
    public void testAuthenticateMandatoryHeaderNotSigned(String signedHeaders, String expectedErrorMessage) throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X-Escher-Date", "20110909T233600Z"),
                new EscherRequest.Header("X-Escher-Auth", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=" + signedHeaders + ", Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("Host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");

        assertAuthenticationError(expectedErrorMessage, request);
    }


    @DataProvider
    public static Object[][] getAuthenticateMandatoryHeaderNotSignedCases() {
        return new Object[][] {
                { "content-type;x-escher-date", "The host header is not signed" },
                { "content-type;host", "The x-escher-date header is not signed" }
        };
    }


    @Test
    public void testAuthenticateInvalidHashAlgo() throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X-Escher-Date", "20110909T233600Z"),
                new EscherRequest.Header("X-Escher-Auth", "EMS-HMAC-SHA128 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-escher-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("Host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");

        assertAuthenticationError("Only SHA256 and SHA512 hash algorithms are allowed", request);
    }


    @Test
    public void testAuthenticateDatesInDateAndAuthHeadersDoNotMatch() throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X-Escher-Date", "20110909T233600Z"),
                new EscherRequest.Header("X-Escher-Auth", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110901/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-escher-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("Host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");

        assertAuthenticationError("The credential date does not match with the request date", request);
    }


    @Test
    @UseDataProvider("getAuthenticateOutsideAcceptedTimeIntervalCases")
    public void testAuthenticateOutsideAcceptedTimeInterval(int clockSkew, int minute) throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X-Escher-Date", "20110909T233600Z"),
                new EscherRequest.Header("X-Escher-Auth", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-escher-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("Host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");
        escher.setClockSkew(clockSkew);
        clock.setInstant(createInstant(2011, 9, 9, 23, minute, 0));

        assertAuthenticationError("The request date is not within the accepted time range", request);
    }


    @DataProvider
    public static Object[][] getAuthenticateOutsideAcceptedTimeIntervalCases() {
        return new Object[][] {
                { 900, 52 },     // clockSkew: 15 min; request is 16 min old
                { 900, 10 },     // clockSkew: 15 min; request is 16 min from the future
                {  60, 38 },     // clockSkew:  1 min; request is  2 min old
                {  60, 34 },     // clockSkew:  1 min; request is  2 min from the future
        };
    }


    @Test
    public void testAuthenticateInvalidCredentialScope() throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X-Escher-Date", "20110909T233600Z"),
                new EscherRequest.Header("X-Escher-Auth", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-2/iam/aws4_request, SignedHeaders=content-type;host;x-escher-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("Host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");

        assertAuthenticationError("The credential scope is invalid", request);
    }


    @Test
    public void testAuthenticateInvalidAccessKeyId() throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X-Escher-Date", "20110909T233600Z"),
                new EscherRequest.Header("X-Escher-Auth", "EMS-HMAC-SHA256 Credential=UNKNOWN/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-escher-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("Host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");

        assertAuthenticationError("Invalid Escher key", request);
    }


    @Test
    public void testAuthenticateInvalidSignature() throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X-Escher-Date", "20110909T233600Z"),
                new EscherRequest.Header("X-Escher-Auth", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-escher-date, Signature=aaac21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("Host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");
        Map<String, String> keyDb = new HashMap<>();
        keyDb.put("AKIDEXAMPLE", "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY");

        try {
            escher.authenticate(request, keyDb);

            fail("exception should have been thrown");
        } catch (EscherException e) {
            assertThat(e.getMessage(), startsWith("The signatures do not match"));
        }
    }


    private void assertAuthenticationError(String expectedErrorMessage, EscherRequest request) {
        Map<String, String> keyDb = new HashMap<>();
        keyDb.put("AKIDEXAMPLE", "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY");

        try {
            escher.authenticate(request, keyDb);

            fail("exception should have been thrown");
        } catch (EscherException e) {
            assertThat(e.getMessage(), startsWith(expectedErrorMessage));
        }
    }

}
