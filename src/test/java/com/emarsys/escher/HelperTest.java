package com.emarsys.escher;


import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HelperTest extends TestBase {

    @Test
    public void testCalculateSigningKey() throws Exception {
        Config config = Config.create()
                .setHashAlgo("sha256")
                .setAlgoPrefix("AWS4")
                .setDate(createDate(2011, Calendar.SEPTEMBER, 9));

        Helper helper = new Helper(config);

        byte[] signingKey = helper.calculateSigningKey(
                "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY",
                "us-east-1/iam/aws4_request"
        );

        assertEquals(
                "98f1d889fec4f4421adc522bab0ce1f82e6929c262ed15e5a94c90efd1e3b0e7",
                DatatypeConverter.printHexBinary(signingKey).toLowerCase()
        );
    }


    @Test
    public void testSigningParams() throws Exception {
        Date date = createDate(2011, Calendar.MAY, 11, 12, 0, 0);
        Config config = Config.create()
                .setAlgoPrefix("EMS")
                .setHashAlgo("SHA256")
                .setDate(date);

        Helper helper = new Helper(config);

        Map<String, String> params = helper.calculateSigningParams("th3K3y", "us-east-1/host/aws4_request", 12345);

        Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put("Algorithm", "EMS-HMAC-SHA256");
        expectedParams.put("Credentials", "th3K3y/20110511/us-east-1/host/aws4_request");
        expectedParams.put("Date", "20110511T120000Z");
        expectedParams.put("Expires", "12345");
        expectedParams.put("SignedHeaders", "host");

        assertEquals(expectedParams, params);
    }

}
