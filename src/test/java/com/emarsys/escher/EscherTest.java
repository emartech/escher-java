package com.emarsys.escher;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(DataProviderRunner.class)
public class EscherTest extends TestBase {


    private Escher escher;


    @Before
    public void setUp() throws Exception {
        escher = new Escher("us-east-1/iam/aws4_request");
    }


    @Test
    public void testSignRequest() throws Exception {
        TestParam param = parseTestData("get-vanilla");

        EscherRequestImpl request = createRequest(param.getRequest());

        TestParam.Config config = param.getConfig();

        Escher escher = new Escher(config.getCredentialScope())
                .setAuthHeaderName(config.getAuthHeaderName())
                .setDateHeaderName(config.getDateHeaderName())
                .setAlgoPrefix(config.getAlgoPrefix())
                .setCurrentTime(getConfigDate(param));

        EscherRequest signedRequest = escher.signRequest(request, config.getAccessKeyId(), config.getApiSecret(), param.getHeadersToSign());

        EscherRequestImpl expectedSignedRequest = createRequest(param.getExpected().getRequest());
        assertEquals("host", expectedSignedRequest.getURI().getHost(), signedRequest.getURI().getHost());
        assertEquals("method", expectedSignedRequest.getHttpMethod(), signedRequest.getHttpMethod());
        assertEquals("path", expectedSignedRequest.getURI().getPath(), signedRequest.getURI().getPath());
        assertEquals("queryParams", expectedSignedRequest.getURI().getQuery(), signedRequest.getURI().getQuery());
        assertEquals("body", expectedSignedRequest.getBody(), signedRequest.getBody());
        assertEquals("headers", expectedSignedRequest.getRequestHeaders(), signedRequest.getRequestHeaders());
    }


    private EscherRequestImpl createRequest(TestParam.Request paramRequest) throws URISyntaxException {
        List<EscherRequest.Header> headers = paramRequest.getHeaders()
                .stream()
                .map(header -> new EscherRequest.Header(header.get(0), header.get(1)))
                .collect(Collectors.toList());

        URI uri = new URI("http://" + paramRequest.getHost() + paramRequest.getUrl());

        return new EscherRequestImpl(paramRequest.getMethod(), uri, headers, paramRequest.getBody());
    }


    @Test
    public void testPresignUrl() throws Exception {
        Escher escher = new Escher("us-east-1/host/aws4_request")
                .setAlgoPrefix("EMS")
                .setVendorKey("EMS")
                .setAuthHeaderName("X-Ems-Auth")
                .setDateHeaderName("X-Ems-Date")
                .setCurrentTime(createDate(2011, Calendar.MAY, 11, 12, 0, 0));

        int expires = 123456;
        String signedUrl = escher.presignUrl("http://example.com/something?foo=bar&baz=barbaz", "th3K3y", "very_secure", expires);

        String expectedSignedUrl = "http://example.com/something?foo=bar&baz=barbaz&X-EMS-Algorithm=EMS-HMAC-SHA256&X-EMS-Credentials=th3K3y%2F20110511%2Fus-east-1%2Fhost%2Faws4_request&X-EMS-Date=20110511T120000Z&X-EMS-Expires=123456&X-EMS-SignedHeaders=host&X-EMS-Signature=fbc9dbb91670e84d04ad2ae7505f4f52ab3ff9e192b8233feeae57e9022c2b67";
        assertEquals(expectedSignedUrl, signedUrl);
    }


    @Test
    public void testAuthenticateSuccess() throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X_EMS_DATE", "20110909T233600Z"),
                new EscherRequest.Header("X_EMS_AUTH", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("CONTENT_TYPE", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");

        Map<String, String> keyDb = new HashMap<>();
        keyDb.put("AKIDEXAMPLE", "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY");

        escher.setAlgoPrefix("EMS")
                .setVendorKey("EMS")
                .setAuthHeaderName("X-Ems-Auth")
                .setDateHeaderName("X-Ems-Date");

        String accessKey = escher.authenticate(request, keyDb);

        assertEquals("AKIDEXAMPLE", accessKey);
    }


    @Test
    @UseDataProvider("getAuthenticationMissingHeaderCases")
    public void testAuthenticateMissingHeader(String headerToExlude, String expectedErrorMessage) throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X_ESCHER_DATE", "20110909T233600Z"),
                new EscherRequest.Header("X_ESCHER_AUTH", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-escher-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("CONTENT_TYPE", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("host", "iam.amazonaws.com")
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
                { "host", "Missing header: host" },
                { "X_ESCHER_DATE", "Missing header: X-Escher-Date" },
                { "X_ESCHER_AUTH", "Missing header: X-Escher-Auth" },
        };
    }


    @Test
    public void testAuthenticateInvalidDateFormat() throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X_ESCHER_DATE", "NOT_A_DATE"),
                new EscherRequest.Header("X_ESCHER_AUTH", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-escher-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("CONTENT_TYPE", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");

        assertAuthenticationError("Invalid date format", request);
    }


    @Test
    @UseDataProvider("getAuthenticateMandatoryHeaderNotSignedCases")
    public void testAuthenticateMandatoryHeaderNotSigned(String signedHeaders, String expectedErrorMessage) throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X_ESCHER_DATE", "20110909T233600Z"),
                new EscherRequest.Header("X_ESCHER_AUTH", "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=" + signedHeaders + ", Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("CONTENT_TYPE", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");

        assertAuthenticationError(expectedErrorMessage, request);
    }


    @DataProvider
    public static Object[][] getAuthenticateMandatoryHeaderNotSignedCases() {
        return new Object[][] {
                { "content-type;x-escher-date", "Host header is not signed" },
                { "content-type;host", "Date header is not signed" }
        };
    }


    @Test
    public void testAuthenticateInvalidHashAlgo() throws Exception {
        List<EscherRequest.Header> headers = Arrays.asList(
                new EscherRequest.Header("X_ESCHER_DATE", "20110909T233600Z"),
                new EscherRequest.Header("X_ESCHER_AUTH", "EMS-HMAC-SHA128 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-escher-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"),
                new EscherRequest.Header("CONTENT_TYPE", "application/x-www-form-urlencoded; charset=utf-8"),
                new EscherRequest.Header("host", "iam.amazonaws.com")
        );
        EscherRequest request = new EscherRequestImpl("POST", new URI("http://iam.amazonaws.com"), headers, "Action=ListUsers&Version=2010-05-08");

        assertAuthenticationError("Only SHA256 and SHA512 hash algorithms are allowed.", request);
    }


    private void assertAuthenticationError(String expectedErrorMessage, EscherRequest request) {
        try {
            escher.authenticate(request, new HashMap<>());

            fail("exception should have been thrown");
        } catch (EscherException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

}