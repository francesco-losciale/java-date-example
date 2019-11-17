package com.java.date.example.javadateexample;

import com.java.date.example.javadateexample.entities.DateExample;
import com.java.date.example.javadateexample.repositories.DateExampleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
class JavaDateExampleApplicationTests {

    @Autowired
    private DateExampleRepository dateExampleRepository;

    // A date-time without a time-zone in the ISO-8601 calendar system,
    // such as 2007-12-03T10:15:30.
    final private LocalDateTime DATETIME_WITHOUT_TIMEZONE =
            LocalDateTime.parse("2011-12-03T10:00:00");

    // A date-time with a time-zone in the ISO-8601 calendar system,
    // such as 2007-12-03T10:15:30+01:00 Europe/Paris.
    final private ZonedDateTime DATETIME_WITH_TIMEZONE =
            DATETIME_WITHOUT_TIMEZONE
                    .atZone(ZoneId.of("GMT+02:00"));

    // A date-time with an offset from UTC/Greenwich in the ISO-8601 calendar system,
    // such as 2007-12-03T10:15:30+01:00.
    final private OffsetDateTime DATETIME_WITH_UTC_OFFSET =
            DATETIME_WITHOUT_TIMEZONE
                    .atOffset(ZoneOffset.ofHours(2));

    private DateExample persistedDateExample;

    @BeforeEach
    void setUp() {
        final DateExample dateExample = createAndSave();
        persistedDateExample = dateExampleRepository.getOne(dateExample.getId());
    }

    @AfterEach
    void tearDown() {
        dateExampleRepository.delete(persistedDateExample);
    }

    @Test
    void when_db_column_with_timestamp_then_Object_saved_properly() {
        assertThat(persistedDateExample.getTimestampWithoutZone())
                .isEqualTo(DATETIME_WITHOUT_TIMEZONE);
    }

    @Test
    void when_db_column_with_Zoned_timestamp_then_Object_saved_properly() {
        assertThat(persistedDateExample.getTimestampWithZone())
                .isEqualTo(DATETIME_WITH_TIMEZONE);
    }

    @Test
    void when_saved_Zoned_timestamp_and_read_value_with_Utc_offset_then_value_is_the_same() {
        assertThat(persistedDateExample.getTimestampWithUtcOffset())
                .isEqualTo(DATETIME_WITH_TIMEZONE.toOffsetDateTime());
    }

    @Test
    void when_System_timezone_is_correct() {
        assertThat(TimeZone.getDefault()).isEqualTo(
                TimeZone.getTimeZone(ZoneId.systemDefault())
        );
    }

    private DateExample createAndSave() {
        DateExample dateExample = new DateExample();
        dateExample.setTimestampWithoutZone(DATETIME_WITHOUT_TIMEZONE);
        dateExample.setTimestampWithZone(DATETIME_WITH_TIMEZONE);
        dateExample.setTimestampWithUtcOffset(DATETIME_WITH_UTC_OFFSET);
        dateExampleRepository.saveAndFlush(dateExample);
        return dateExample;
    }
}
