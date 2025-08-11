package com.emarsys.escher.testcase;

import com.emarsys.escher.Escher;
import com.emarsys.escher.EscherException;
import com.emarsys.escher.EscherRequest;
import com.emarsys.escher.EscherRequestImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@RunWith(DataProviderRunner.class)
public class EscherAuthEndToEndTest {

    private static final List<String> ignoredTestCases = Arrays.asList(
            // cannot happen because of strong typing
            "test-cases/ducktype_cases/authenticate-error-mandatoryheaders-not-array.json",
            // cannot happen because of strong typing
            "test-cases/ducktype_cases/authenticate-error-mandatoryheaders-not-array-of-strings.json",
            // cannot happen because not valid URI
            "test-cases/test_cases/authenticate-error-invalid-request-url.json",
            // cannot happen because not valid URI
            "test-cases/test_cases/signrequest-error-invalid-request-url.json",
            // cannot happen because not valid URI
            "test-cases/aws4_testsuite/signrequest-post-vanilla-query-nonunreserved.json"
    );

    @Test
    @UseDataProvider("signRequestEscher")
    public void signRequest(TestCase testCase) throws URISyntaxException {
        Escher escher = getEscher(testCase);
        EscherRequest request = getEscherRequest(testCase.getRequest());

        try {
            EscherRequest signedRequest = escher.signRequest(
                    request,
                    testCase.getConfig().getAccessKeyId(),
                    testCase.getConfig().getApiSecret(),
                    testCase.getHeadersToSign()
            );

            if (testCase.getExpected().getCanonicalizedRequest() != null) {
                assertEquals(testCase.getExpected().getCanonicalizedRequest(), escher.debugInfo.get("canonicalizedRequest"));
            }
            if (testCase.getExpected().getStringToSign() != null) {
                assertEquals(testCase.getExpected().getStringToSign(), escher.debugInfo.get("stringToSign"));
            }
            if (testCase.getExpected().getAuthHeader() != null) {
                assertEquals(testCase.getExpected().getAuthHeader(), escher.debugInfo.get("authHeaderValue"));
            }
            EscherRequest expectedRequest = getEscherRequest(testCase.getExpected().getRequest());
            assertEquals(expectedRequest, signedRequest);
        } catch (EscherException e) {
            assertEquals(testCase.getExpected().getError(), e.getMessage());
        }
    }

    @Test
    @UseDataProvider("authenticateEscher")
    public void authenticate(TestCase testCase) throws URISyntaxException {
        Escher escher = getEscher(testCase);
        EscherRequest request = getEscherRequest(testCase.getRequest());

        try {
            String apiKey = escher.authenticate(request, testCase.getKeyDbAsMap(), testCase.getMandatorySignedHeaders());
            assertEquals(testCase.getExpected().getApiKey(), apiKey);
        } catch (EscherException e) {
            assertEquals(testCase.getExpected().getError(), e.getMessage());
        }
    }

    @Test
    @UseDataProvider("presignUrlEscher")
    public void presignUrl(TestCase testCase) throws EscherException {
        Escher escher = getEscher(testCase);

        String url = escher.presignUrl(
                testCase.getRequest().getUrl(),
                testCase.getConfig().getAccessKeyId(),
                testCase.getConfig().getApiSecret(),
                testCase.getRequest().getExpires()
        );

        assertEquals(testCase.getExpected().getUrl(), url);
    }

    @DataProvider
    public static List<TestCase> signRequestEscher() throws IOException {
        return collectTestCases(TestCaseType.SIGN_REQUEST);
    }

    @DataProvider
    public static List<TestCase> authenticateEscher() throws IOException {
        return collectTestCases(TestCaseType.AUTHENTICATE);
    }

    @DataProvider
    public static List<TestCase> presignUrlEscher() throws IOException {
        return collectTestCases(TestCaseType.PRESIGN_URL);
    }

    private static Escher getEscher(TestCase testCase) {
        Instant instant = testCase.getConfig().getDateAsInstant();
        ZoneId zoneId = ZoneId.of("Etc/UTC");
        Escher escher = new Escher(testCase.getConfig().getCredentialScope(), Clock.fixed(instant, zoneId));
        escher.setVendorKey(testCase.getConfig().getVendorKey())
                .setAlgoPrefix(testCase.getConfig().getAlgoPrefix())
                .setAuthHeaderName(testCase.getConfig().getAuthHeaderName());
        if (testCase.getConfig().getHashAlgo() != null) {
            escher.setHashAlgo(testCase.getConfig().getHashAlgo());
        }
        if (testCase.getConfig().getDateHeaderName() != null) {
            escher.setDateHeaderName(testCase.getConfig().getDateHeaderName());
        }
        return escher;
    }

    private static EscherRequestImpl getEscherRequest(Request request) throws URISyntaxException {
        return new EscherRequestImpl(
                request.getMethod(),
                new URI("http://localhost" + request.getUrl()),
                request.getHeadersAsHeaders(),
                request.getBody()
        );
    }

    private static List<TestCase> collectTestCases(TestCaseType type) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (Stream<Path> walk = Files.walk(Paths.get(TestCase.TEST_CASES_FOLDER))) {
            return walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .filter(path -> !path.getParent().getFileName().toString().equals(TestCase.EXCLUDED_FOLDER))
                    .filter(path -> path.getFileName().toString().startsWith(type.getPrefix()))
                    .filter(path -> !ignoredTestCases.contains(path.toString()))
                    .map(path -> {
                        try {
                            return mapper.readValue(path.toFile(), TestCase.class);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }
}
