package com.emarsys.escher;


import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.bind.DatatypeConverter;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(DataProviderRunner.class)
public class HelperTest extends TestBase {

    @Test
    public void testCalculateSigningKey() throws Exception {
        Config config = Config.create()
                .setHashAlgo("sha256")
                .setAlgoPrefix("AWS4");

        Helper helper = new Helper(config);

        byte[] signingKey = helper.calculateSigningKey(
                "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY",
                createInstant(2011, 9, 9), "us-east-1/iam/aws4_request"
        );

        assertThat(
                DatatypeConverter.printHexBinary(signingKey).toLowerCase(),
                is("98f1d889fec4f4421adc522bab0ce1f82e6929c262ed15e5a94c90efd1e3b0e7")
        );
    }


    @Test
    public void testSigningParams() throws Exception {
        Instant date = createInstant(2011, 5, 11, 12, 0, 0);
        Config config = Config.create()
                .setAlgoPrefix("EMS")
                .setHashAlgo("SHA256");

        Helper helper = new Helper(config);

        Map<String, String> params = helper.calculateSigningParams("th3K3y", date, "us-east-1/host/aws4_request", 12345);

        Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put("Algorithm", "EMS-HMAC-SHA256");
        expectedParams.put("Credentials", "th3K3y/20110511/us-east-1/host/aws4_request");
        expectedParams.put("Date", "20110511T120000Z");
        expectedParams.put("Expires", "12345");
        expectedParams.put("SignedHeaders", "host");

        assertThat(params, is(expectedParams));
    }


    @Test
    public void testCanonicalizeWithMoreHeaderThanHeadersToSign() throws Exception {
        TestParam param = parseTestData("get-vanilla");

        TestParam.Request paramRequest = param.getRequest();

        List<EscherRequest.Header> headers = paramRequest.getHeaders()
                .stream()
                .map(header -> new EscherRequest.Header(header.get(0), header.get(1)))
                .collect(Collectors.toList());

        URI uri = new URI("http://" + paramRequest.getHost() + paramRequest.getUrl());

        EscherRequestImpl request = new EscherRequestImpl(paramRequest.getMethod(), uri, headers, paramRequest.getBody());
        request.addHeader("Custom-Header", "should-not-be-signed");

        Helper helper = new Helper(createConfig(param));

        String canonicalized = helper.canonicalize(request, param.getHeadersToSign());

        assertThat(canonicalized, is(param.getExpected().getCanonicalizedRequest()));
    }


    @Test
    @UseDataProvider("getAddMandatoryHeadersCases")
    public void testAddMandatoryHeaders(List<String> missingHeaders, String expectedHost, String expectedDate) throws Exception {
        Config config = Config.create();
        config.setDateHeaderName("test-date");

        List<EscherRequest.Header> headers = new ArrayList<>();
        headers.add(new EscherRequest.Header("test-date", "given date"));
        headers.add(new EscherRequest.Header("host", "given host"));
        missingHeaders.forEach(headerNameToRemove -> headers.removeIf(header -> header.getFieldName().equals(headerNameToRemove)));

        EscherRequest request = new EscherRequestImpl("GET", new URI("http://example.com/something"), headers, "");

        Helper helper = new Helper(config);

        helper.addMandatoryHeaders(request, createInstant(2011, 5, 11, 12, 0, 0));

        Collections.sort(headers, (h1, h2) -> h1.getFieldName().compareTo(h2.getFieldName()));
        assertThat(headers.size(), is(2));
        assertThat(headers.get(0).getFieldValue(), is(expectedHost));
        assertThat(headers.get(1).getFieldValue(), is(expectedDate));
    }


    @DataProvider
    public static Object[][] getAddMandatoryHeadersCases() {
        return new Object[][] {
                { Arrays.asList("test-date"), "given host", "20110511T120000Z" },
                { Arrays.asList("host"), "example.com", "given date" },
                { Arrays.asList("test-date", "host"), "example.com", "20110511T120000Z" },
        };
    }


    @Test
    @UseDataProvider("getAddMandatoryHeadersWithPortNumberCases")
    public void testAddMandatoryHeadersWithPortNumber(String url, String expectedHost) throws Exception {
        Config config = Config.create();
        config.setDateHeaderName("test-date");

        List<EscherRequest.Header> headers = new ArrayList<>();
        headers.add(new EscherRequest.Header("test-date", "given date"));

        EscherRequest request = new EscherRequestImpl("GET", new URI(url), headers, "");

        Helper helper = new Helper(config);

        helper.addMandatoryHeaders(request, Instant.now());

        EscherRequest.Header hostHeader = headers
                .stream()
                .filter(header -> header.getFieldName().equals("host"))
                .findFirst()
                .get();

        assertThat(hostHeader.getFieldValue(), is(expectedHost));
    }


    @DataProvider
    public static Object[][] getAddMandatoryHeadersWithPortNumberCases() {
        return new Object[][] {
                { "http://example.com/something", "example.com" },
                { "http://example.com:123/something", "example.com:123" },
                { "http://example.com:80/something", "example.com" },
                { "http://example.com:443/something", "example.com:443" },
                { "https://example.com/something", "example.com" },
                { "https://example.com:123/something", "example.com:123" },
                { "https://example.com:443/something", "example.com" },
                { "https://example.com:80/something", "example.com:80" }
        };
    }


    @Test
    public void testParseAuthElementsWithAuthHeaderAndSignatureQueryParam() throws Exception {
        Config config = Config.create();
        String authHeader = "ESR-HMAC-SHA256 Credential=AKID-EXAMPLE-HEADER/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd";
        String url = "http://example.com?X-TEST-Algorithm=ESR-HMAC-SHA256&X-TEST-Credentials=AKID-EXAMPLE-QUERY%2F20110909%2Fus-east-1%2Fiam%2Faws4_request&X-TEST-Date=20111009T000000Z&X-TEST-Expires=10&X-TEST-SignedHeaders=host&X-TEST-Signature=191c13fb57e5ff0b8c0ad30fe94f4f0be40b3865916cf6ef084fffd67bae6239";

        EscherRequest request = new EscherRequestImpl("GET", new URI(url), new ArrayList<>(), "");
        request.addHeader(config.getAuthHeaderName(), authHeader);

        Helper helper = new Helper(config);
        AuthElements authElements = helper.parseAuthElements(request);

        assertThat(authElements.getAccessKeyId(), is("AKID-EXAMPLE-HEADER"));
    }


    @Test
    public void testParseAuthElementsWithAuthHeaderOnly() throws Exception {
        Config config = Config.create();
        String authHeader = "ESR-HMAC-SHA256 Credential=AKID-EXAMPLE-HEADER/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd";
        String url = "http://example.com";

        EscherRequest request = new EscherRequestImpl("GET", new URI(url), new ArrayList<>(), "");
        request.addHeader(config.getAuthHeaderName(), authHeader);

        Helper helper = new Helper(config);
        AuthElements authElements = helper.parseAuthElements(request);

        assertThat(authElements.getAccessKeyId(), is("AKID-EXAMPLE-HEADER"));
    }


    @Test
    public void testParseAuthElementsWithSignatureQueryOnly() throws Exception {
        Config config = Config.create();
        String url = "http://example.com?X-Escher-Algorithm=ESR-HMAC-SHA256&X-Escher-Credentials=AKID-EXAMPLE-QUERY%2F20110909%2Fus-east-1%2Fiam%2Faws4_request&X-Escher-Date=20111009T000000Z&X-Escher-Expires=10&X-Escher-SignedHeaders=host&X-Escher-Signature=191c13fb57e5ff0b8c0ad30fe94f4f0be40b3865916cf6ef084fffd67bae6239";

        EscherRequest request = new EscherRequestImpl("GET", new URI(url), new ArrayList<>(), "");

        Helper helper = new Helper(config);
        AuthElements authElements = helper.parseAuthElements(request);

        assertThat(authElements.getAccessKeyId(), is("AKID-EXAMPLE-QUERY"));
    }


    @Test
    public void testParseAuthElementsWithoutAnySignatures() throws Exception {
        Config config = Config.create();
        String url = "http://example.com";

        EscherRequest request = new EscherRequestImpl("GET", new URI(url), new ArrayList<>(), "");

        Helper helper = new Helper(config);

        try {
            helper.parseAuthElements(request);
            fail("exception should have been thrown");
        } catch (EscherException e) {
            assertThat(e.getMessage(), is("Request has not been signed."));
        }
    }


    @Test
    public void testParseDateFromHeader() throws Exception {
        Config config = Config.create();
        config.setDateHeaderName("DATE");
        String url = "http://example.com";

        EscherRequest request = new EscherRequestImpl("GET", new URI(url), new ArrayList<>(), "");
        request.addHeader("DATE", "20111009T000000Z");
        request.addHeader(config.getAuthHeaderName(), "whatever");

        Helper helper = new Helper(config);
        Instant date = helper.parseDate(request);

        assertThat(date.atOffset(ZoneOffset.UTC).getYear(), is(2011));
    }


    @Test
    public void testParseDateFromHeaderWhenThereIsNoDate() throws Exception {
        Config config = Config.create();
        String url = "http://example.com";

        EscherRequest request = new EscherRequestImpl("GET", new URI(url), new ArrayList<>(), "");
        request.addHeader(config.getAuthHeaderName(), "whatever");

        Helper helper = new Helper(config);

        try {
            helper.parseDate(request);
            fail("exception should have been thrown");
        } catch (EscherException ignored) {}
    }


    @Test
    public void testParseDateFromQueryString() throws Exception {
        Config config = Config.create();
        String url = "http://example.com?X-Escher-Date=20111009T000000Z";

        EscherRequest request = new EscherRequestImpl("GET", new URI(url), new ArrayList<>(), "");

        Helper helper = new Helper(config);
        Instant date = helper.parseDate(request);

        assertThat(date.atOffset(ZoneOffset.UTC).getYear(), is(2011));
    }


    @Test
    public void testParseDateFromQueryStringWhenThereIsNoDate() throws Exception {
        Config config = Config.create();
        String url = "http://example.com";

        EscherRequest request = new EscherRequestImpl("GET", new URI(url), new ArrayList<>(), "");

        Helper helper = new Helper(config);

        try {
            helper.parseDate(request);
            fail("exception should have been thrown");
        } catch (EscherException ignored) {}
    }

}
