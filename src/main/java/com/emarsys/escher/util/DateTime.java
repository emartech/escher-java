package com.emarsys.escher.util;

import com.emarsys.escher.EscherException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTime {

    public static final TimeZone TIMEZONE = TimeZone.getTimeZone("UTC");
    public static final String LONG_DATE_FORMAT = "yyyyMMdd'T'HHmmss'Z'";
    public static final String SHORT_DATE_FORMAT = "yyyyMMdd";


    public static String toLongString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(LONG_DATE_FORMAT);
        dateFormat.setTimeZone(TIMEZONE);
        return dateFormat.format(date);
    }


    public static String toShortString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(SHORT_DATE_FORMAT);
        dateFormat.setTimeZone(TIMEZONE);
        return dateFormat.format(date);
    }


    public static Date parseLongString(String text) throws EscherException {
        return parse(text, LONG_DATE_FORMAT);
    }


    public static Date parseShortString(String text) throws EscherException {
        return parse(text, SHORT_DATE_FORMAT);
    }


    private static Date parse(String text, String format) throws EscherException {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);
            return dateFormat.parse(text);
        } catch (ParseException e) {
            throw new EscherException("Invalid date format");
        }
    }


    public static boolean sameDay(Date date1, Date date2) {
        Calendar cal1 = toCalendar(date1);
        Calendar cal2 = toCalendar(date2);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }


    public static Date subtractSeconds(Date date, int seconds) {
        return addSeconds(date, -seconds);
    }


    public static Date addSeconds(Date date, int seconds) {
        Calendar calendar = toCalendar(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }


    private static Calendar toCalendar(Date date) {
        Calendar calendar = Calendar.getInstance(DateTime.TIMEZONE);
        calendar.setTime(date);
        return calendar;
    }

}
