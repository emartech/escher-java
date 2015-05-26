package com.emarsys.escher;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.junit.Assert.fail;


@RunWith(DataProviderRunner.class)
public class AuthenticationValidatorTest extends TestBase {

    private Config config;
    private Date currentTime;


    @Before
    public void setUp() throws Exception {
        config = Config.create().setClockSkew(10);
        currentTime = createDate(2011, 9, 9, 12, 0, 25);
    }


    @Test
    @UseDataProvider("getDatesInValidTimeRange")
    public void testValidateDates(Date date) throws Exception {
        AuthenticationValidator validator = new AuthenticationValidator(config);
        validator.validateDates(date, date, currentTime, 0);
    }


    @DataProvider
    public static Object[][] getDatesInValidTimeRange() {
        return new Object[][] {
                { createDate(2011, 9, 9, 12, 0, 25) },     // same as current time
                { createDate(2011, 9, 9, 12, 0, 34) },     // just below the upper border
                { createDate(2011, 9, 9, 12, 0, 16) },     // just above the lower border
        };
    }


    @Test
    @UseDataProvider("getDatesOutOfValidTimeRange")
    public void testValidateDatesOutSiteValidRange(Date date) throws Exception {
        AuthenticationValidator validator = new AuthenticationValidator(config);
        try {
            validator.validateDates(date, date, currentTime, 0);
            fail("exception should have been thrown");
        } catch (EscherException ignored) {}
    }


    @DataProvider
    public static Object[][] getDatesOutOfValidTimeRange() {
        return new Object[][] {
                { createDate(2011, 9,  9, 12, 0, 36) },     // just above the upper border
                { createDate(2011, 9,  9, 12, 0, 14) },     // just below the lower border
                { createDate(2011, 9, 10, 12, 0, 25) },     // good time, but other day
        };
    }


    @Test
    public void testValidateWhenRequestAndCredentialDatesDoNotMatch() throws Exception {
        Date requestDate = createDate(2011, 9, 9, 12, 0, 25);
        Date credentialDate = createDate(2011, 9, 10);

        AuthenticationValidator validator = new AuthenticationValidator(config);
        try {
            validator.validateDates(requestDate, credentialDate, currentTime, 0);
            fail("exception should have been thrown");
        } catch (EscherException ignored) {}
    }


    @Test
    public void testValidateDatesWithExpirationGiven() throws Exception {
        Date requestDate = createDate(2011, 9, 9, 12, 0, 6);
        Date credentialDate = createDate(2011, 9, 9);

        AuthenticationValidator validator = new AuthenticationValidator(config);
        validator.validateDates(requestDate, credentialDate, currentTime, 10);
    }


    @Test
    public void testValidateDatesWithExpirationPassed() throws Exception {
        Date requestDate = createDate(2011, 9, 9, 12, 0, 46);
        Date credentialDate = createDate(2012, 9, 9);

        AuthenticationValidator validator = new AuthenticationValidator(config);
        try {
            validator.validateDates(requestDate, credentialDate, currentTime, 10);
            fail("exception should have been thrown");
        } catch (EscherException ignored) {}
    }

}