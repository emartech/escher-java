package com.emarsys.escher;


import org.junit.Test;

import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;

public class HelperTestCalculateSigningKey {

    @Test
    public void testCalculateSigningKey() throws Exception {
        String signingKey = Helper.calculateSigningKey(
                "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY",
                new GregorianCalendar(2011, 8, 9).getTime(),
                "us-east-1/iam/aws4_request",
                "sha256",
                "AWS4"
        );

        assertEquals(
                "98f1d889fec4f4421adc522bab0ce1f82e6929c262ed15e5a94c90efd1e3b0e7",
                signingKey
        );
    }

}
