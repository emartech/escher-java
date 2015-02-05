package com.emarsys.escher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class HelperTestWithTestData extends TestBase {

    private String fileName;
    private TestParam param;


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        String[] fileList = new String[] {
                "get-vanilla",
                "post-vanilla",
                "get-vanilla-query",
                "post-vanilla-query",
                "get-vanilla-empty-query-key",
                "post-vanilla-empty-query-value",
                "get-vanilla-query-order-key",
                "post-x-www-form-urlencoded",
                "post-x-www-form-urlencoded-parameters",

                "get-header-value-trim",
                "post-header-key-case",
                "post-header-key-sort",
                "post-header-value-case",

                "get-vanilla-query-order-value",
                "get-vanilla-query-order-key-case",
                "get-unreserved",
//                "get-vanilla-query-unreserved",
//                "get-vanilla-ut8-query",
                "get-utf8",
                "get-space",
//                "post-vanilla-query-space",
//                "post-vanilla-query-nonunreserved",

//                "get-slash",
//                "get-slashes",
//                "get-slash-dot-slash",
//                "get-slash-pointless-dot",
//                "get-relative",
//                "get-relative-relative",
        };

        ArrayList<Object[]> testCases = new ArrayList<>();
        for (String fileName : fileList) {
            testCases.add(new String[] { fileName });
        }
        return testCases;
    }


    public HelperTestWithTestData(String fileName) {
        this.fileName = fileName;
    }


    @Before
    public void setUp() throws Exception {
        param = parseTestData(fileName);
    }


    @Test
    public void testCanonicalize() throws Exception {

        TestParam.Request paramRequest = param.getRequest();

        List<NameValuePair> headers = new ArrayList<>();
        for (List<String> header : paramRequest.getHeaders()) {
            headers.add(new BasicNameValuePair(header.get(0), header.get(1)));
        }

        URI uri = new URI("http://" + paramRequest.getHost() + paramRequest.getUrl());

        Request request = new Request(paramRequest.getMethod(), uri, headers, paramRequest.getBody());

        String canonicalised = Helper.canonicalize(request);

        assertEquals(fileName, param.getExpected().getCanonicalizedRequest(), canonicalised);
    }


    @Test
    public void testCalculateStringToSign() throws Exception {
        String stringToSign = Helper.calculateStringToSign(param.getConfig().getCredentialScope(),
                param.getExpected().getCanonicalizedRequest(),
                getConfigDate(),
                param.getConfig().getHashAlgo(),
                param.getConfig().getAlgoPrefix());
        assertEquals(fileName, param.getExpected().getStringToSign(), stringToSign);
    }


    private Date getConfigDate() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.parse(param.getConfig().getDate());
    }


    @Test
    public void testCalculateAuthHeader() throws Exception {
        byte[] signingKey = Helper.calculateSigningKey(
                param.getConfig().getApiSecret(),
                getConfigDate(),
                param.getConfig().getCredentialScope(),
                param.getConfig().getHashAlgo(),
                param.getConfig().getAlgoPrefix()
        );
        String authHeader = Helper.calculateAuthHeader(
                param.getConfig().getAccessKeyId(),
                getConfigDate(),
                param.getConfig().getCredentialScope(),
                signingKey,
                param.getConfig().getHashAlgo(),
                param.getConfig().getAlgoPrefix(),
                param.getHeadersToSign(),
                param.getExpected().getStringToSign()
        );
        assertEquals(fileName, param.getExpected().getAuthHeader(), authHeader);
    }

}