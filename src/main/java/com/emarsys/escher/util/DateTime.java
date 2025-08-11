package com.emarsys.escher.util;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.time.format.DateTimeFormatter.ofPattern;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import com.emarsys.escher.EscherException;

public class DateTime {

    private static final DateTimeFormatter LONG = ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(UTC);
    private static final DateTimeFormatter SHORT = ofPattern("yyyyMMdd").withZone(UTC);
    private static final DateTimeFormatter HEADER = ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"));

    public static String toLongString(Instant date) {
        return LONG.format(date);
    }

    public static String toHeaderString(Instant date) {
        return HEADER.format(date);
    }

    public static String toShortString(Instant date) {
        return SHORT.format(date);
    }


    public static Instant parseLongString(String text) throws EscherException {
        try {
            return Instant.from(LONG.parse(text));
        } catch (DateTimeParseException e) {
            try {
                return Instant.from(RFC_1123_DATE_TIME.parse(text));
            } catch (DateTimeParseException ex) {
                throw new EscherException("Invalid date format", ex);
            }
        }
    }


    public static Instant parseShortString(String text) throws EscherException {
        try {
            return LocalDate.from(SHORT.parse(text)).atStartOfDay(UTC).toInstant();
        } catch (DateTimeParseException e) {
            throw new EscherException("Invalid date format", e);
        }
    }


    public static boolean sameDay(Instant requestDate, Instant credentialDate) {
        Instant a = requestDate.truncatedTo(ChronoUnit.DAYS);
        Instant b = credentialDate.truncatedTo(ChronoUnit.DAYS);
        return a.equals(b);
    }


}
