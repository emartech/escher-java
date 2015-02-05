package com.emarsys.escher;


import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TestBase {


    public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");


    protected TestParam parseTestData(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("src/test/fixtures/aws4_testsuite/" + fileName + ".json"), TestParam.class);
    }


    protected Date getConfigDate(TestParam param) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(UTC_TIMEZONE);
        return dateFormat.parse(param.getConfig().getDate());
    }


    protected Date createDate(int year, int month, int day) {
        return createDate(year, month, day, 0, 0, 0);
    }


    protected Date createDate(int year, int month, int day, int hourOfDay, int minute, int second) {
        GregorianCalendar calendar = new GregorianCalendar(UTC_TIMEZONE);
        calendar.set(year, month, day, hourOfDay, minute, second);
        return calendar.getTime();
    }

}
