package com.emarsys.escher;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class HelperTest {

    private String fileName;
    private TestParam param;


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        String[] fileList = new String[] {
                "src/test/fixtures/aws4_testsuite/get-vanilla.json"
        };

        ArrayList<Object[]> testCases = new ArrayList<>();
        for (String fileName : fileList) {
            testCases.add(new String[] { fileName });
        }
        return testCases;
    }


    public HelperTest(String fileName) {
        this.fileName = fileName;
    }


    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        param = mapper.readValue(new File(this.fileName), TestParam.class);
    }


    @Test
    public void testCanonicalize() throws Exception {
        TestParam.Request paramRequest = param.getRequest();

        List<String[]> headers = new ArrayList<>();
        for (List<String> header : paramRequest.getHeaders()) {
            headers.add(new String[] {header.get(0), header.get(1)});
        }

        Map<String, String> params = new HashMap<>();

        Request request = new Request(paramRequest.getMethod(), headers, paramRequest.getHost(), paramRequest.getUrl(), params, paramRequest.getBody());

        String canonicalised = Helper.canonicalize(request);

        assertEquals(param.getExpected().getCanonicalizedRequest(), canonicalised);
    }


    @Test
    public void testCalculateStringToSign() throws Exception {
        String stringToSign = Helper.calculateStringToSign(param.getConfig().getCredentialScope(),
                param.getExpected().getCanonicalizedRequest(),
                getConfigDate(),
                param.getConfig().getHashAlgo(),
                param.getConfig().getAlgoPrefix());
        assertEquals(param.getExpected().getStringToSign(), stringToSign);
    }


    private Date getConfigDate() throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(param.getConfig().getDate());
    }


    @Test
    public void testCalculateAuthHeader() throws Exception {
        String signingKey = Helper.calculateSigningKey(
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
                DatatypeConverter.parseHexBinary(signingKey),
                param.getConfig().getHashAlgo(),
                param.getConfig().getAlgoPrefix(),
                param.getHeadersToSign(),
                param.getExpected().getStringToSign()
        );
        assertEquals(param.getExpected().getAuthHeader(), authHeader);
    }

}