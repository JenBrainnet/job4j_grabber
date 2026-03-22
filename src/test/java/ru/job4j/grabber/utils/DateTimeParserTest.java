package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;

class DateTimeParserTest {

    @Test
    void whenValidDateThenParseCorrectly() {
        DateTimeParser parser = new DateTimeParser();
        String dateToParse = "2026-02-24T12:07:22";
        LocalDateTime result = parser.parse(dateToParse);
        LocalDateTime expected = LocalDateTime.of(2026, 2, 24, 12, 7, 22);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenDateWithZoneThenZoneIgnored() {
        DateTimeParser parser = new DateTimeParser();
        String dateToParse = "2026-02-24T12:07:22+03:00";
        LocalDateTime result = parser.parse(dateToParse);
        LocalDateTime expected = LocalDateTime.of(2026, 2, 24, 12, 7, 22);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenInvalidDateFormatThenExceptionThrown() {
        DateTimeParser parser = new DateTimeParser();
        String dateToParse = "not a date";
        assertThatThrownBy(() -> parser.parse(dateToParse))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void whenDateIsEmptyFormatThenExceptionThrown() {
        DateTimeParser parser = new DateTimeParser();
        String dateToParse = "";
        assertThatThrownBy(() -> parser.parse(dateToParse))
                .isInstanceOf(DateTimeParseException.class);
    }

}