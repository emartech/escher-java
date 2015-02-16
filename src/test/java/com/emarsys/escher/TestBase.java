package com.emarsys.escher;


import com.emarsys.escher.util.DateTime;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestBase {


    protected TestParam parseTestData(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("src/test/fixtures/aws4_testsuite/" + fileName + ".json"), TestParam.class);
    }


    protected Date getConfigDate(TestParam param) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(DateTime.TIMEZONE);
        return dateFormat.parse(param.getConfig().getDate());
    }


    protected Date createDate(int year, int month, int day) {
        return createDate(year, month, day, 0, 0, 0);
    }


    protected Date createDate(int year, int month, int day, int hourOfDay, int minute, int second) {
        Calendar calendar = Calendar.getInstance(DateTime.TIMEZONE);
        calendar.set(year, month, day, hourOfDay, minute, second);
        return calendar.getTime();
    }


    protected Config createConfig(TestParam param) throws Exception{
        return Config.create()
                    .setAlgoPrefix(param.getConfig().getAlgoPrefix())
                    .setHashAlgo(param.getConfig().getHashAlgo())
                    .setDate(getConfigDate(param));
    }

}
