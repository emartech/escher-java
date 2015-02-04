package com.emarsys.escher;


import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HelperTest {

    @Test
    public void testCalculateSigningKey() throws Exception {
        byte[] signingKey = Helper.calculateSigningKey(
                "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY",
                new GregorianCalendar(2011, 8, 9).getTime(),
                "us-east-1/iam/aws4_request",
                "sha256",
                "AWS4"
        );

        assertEquals(
                "98f1d889fec4f4421adc522bab0ce1f82e6929c262ed15e5a94c90efd1e3b0e7",
                DatatypeConverter.printHexBinary(signingKey).toLowerCase()
        );
    }


    @Test
    public void testSigningParams() throws Exception {

        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2011-05-11 12:00:00");

        Map<String, String> params = Helper.calculateSigningParams("EMS", "SHA256", "th3K3y", date, "us-east-1/host/aws4_request", 12345);

        Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put("Algorithm", "EMS-HMAC-SHA256");
        expectedParams.put("Credentials", "th3K3y/20110511/us-east-1/host/aws4_request");
        expectedParams.put("Date", "20110511T120000Z");
        expectedParams.put("Expires", "12345");
        expectedParams.put("SignedHeaders", "host");

        assertEquals(expectedParams, params);
    }
}
