package com.emarsys.escher.util;

import com.emarsys.escher.EscherException;
import org.junit.Assert;
import org.junit.Test;


public class HmacTest {

    @Test
    public void testHashGenerationSHA256() throws EscherException {
        String textToHash = "test text";
        String expectedHash = "0f46738ebed370c5c52ee0ad96dec8f459fb901c2ca4e285211eddf903bf1598";

        Assert.assertEquals(expectedHash, Hmac.hash(textToHash));
    }

}