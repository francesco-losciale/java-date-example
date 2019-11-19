package com.java.date.example.javadateexample;

import com.java.date.example.javadateexample.entities.DateExample;
import com.java.date.example.javadateexample.repositories.DateExampleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.jdbc.time_zone = Europe/London"})
@Rollback(false)
class JavaDateTime1_With_EuropeLondon_SessionDatabaseTimezone_Test {

    @Autowired
    private DateExampleRepository dateExampleRepository;

    // Edge case: The time distance between Paris and UTC/GMT/London is 1 hour.
    // Be careful when you see values saved in a table WITH ZONE
    // During the Daylight Saving Time (31 March to 27 October in UK)
    // The UTC offset from Paris is +02:00 hours instead of only one

    final ZonedDateTime parisZoned = ZonedDateTime.parse("2019-12-27T11:39:34.326+00:00[Europe/Paris]");
    final OffsetDateTime parisUtc = parisZoned.toOffsetDateTime();
    final LocalDateTime parisLocal = parisZoned.toLocalDateTime();
    final ZonedDateTime londonZonedSameParisInstant = parisZoned.withZoneSameInstant(ZoneId.of("Europe/London"));

    private static DateExample dateExample = null;

    @BeforeEach
    void setUp() {
        if (dateExample == null) {
            dateExample = new DateExample();
            dateExample.setTimestampWithoutZone(parisLocal);
            dateExample.setTimestampWithZone(parisZoned);
            dateExample.setTimestampWithUtcOffset(parisUtc);
            dateExampleRepository.saveAndFlush(dateExample);
        }
    }

    @Test
    void when_Converting_EuropeParis_To_Utc_Then_Offset_is_Two_Hours() {
        assertThat(parisUtc).isEqualTo(OffsetDateTime.parse("2019-12-27T11:39:34.326+01:00"));
        assertThat(parisUtc.getOffset()).isEqualTo(ZoneOffset.ofHours(1));
    }

    @Test
    void when_Converting_EuropeParis_To_Local_EuropeLondon_Then_Offset_Is_Ignored() {
        assertThat(parisLocal).isEqualTo(LocalDateTime.parse("2019-12-27T11:39:34.326"));
    }

    @Test
    void when_Converting_EuropeParis_To_Zoned_EuropeLondon_Then_One_Hour_Earlier() {
        assertThat(londonZonedSameParisInstant.toOffsetDateTime()).isEqualTo(OffsetDateTime.parse("2019-12-27T10:39:34.326Z"));
        assertThat(londonZonedSameParisInstant.toOffsetDateTime().getOffset()).isEqualTo(ZoneOffset.ofHours(0));
    }

    @Test
    void when_Reading_DateTime_From_Database_In_London_Then_Expect_Correct_Values() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/London")));
        final List<DateExample> dateExampleList = dateExampleRepository.findAll();
        final DateExample dateExample = dateExampleList.get(dateExampleList.size()-1);

        // converting from UTC to our ZoneTime the instant will be the same
        assertThat(dateExample.getTimestampWithZone()).isEqualTo("2019-12-27T10:39:34.326Z[Europe/London]");
        assertThat(dateExample.getTimestampWithUtcOffset()).isEqualTo("2019-12-27T10:39:34.326Z");

        // Reading Without Zone datetime we can see the instant is not the correct one
        assertThat(dateExample.getTimestampWithoutZone()).isNotEqualTo("2019-12-27T10:39:34.326");
        assertThat(dateExample.getTimestampWithoutZone()).isEqualTo("2019-12-27T11:39:34.326");
    }

    @Test
    void when_Reading_DateTime_From_Database_In_Tokyo_Then_Expect_Correct_Values() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Tokyo")));
        final List<DateExample> dateExampleList = dateExampleRepository.findAll();
        final DateExample dateExample = dateExampleList.get(dateExampleList.size()-1);

        // converting from UTC to our ZoneTime the instant will be the same
        assertThat(dateExample.getTimestampWithZone()).isEqualTo("2019-12-27T18:39:34.326+00:00[Asia/Tokyo]");
        assertThat(dateExample.getTimestampWithUtcOffset()).isEqualTo("2019-12-27T18:39:34.326+09:00");

        // Reading Without Zone datetime we can see the instant is not the correct one
        assertThat(dateExample.getTimestampWithoutZone()).isNotEqualTo("2019-12-27T18:39:34.326");
        assertThat(dateExample.getTimestampWithoutZone()).isEqualTo("2019-12-27T11:39:34.326");
    }

    @Test
    void when_Reading_DateTime_From_Database_In_Paris_Then_Expect_Correct_Values() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/Paris")));
        final List<DateExample> dateExampleList = dateExampleRepository.findAll();
        final DateExample dateExample = dateExampleList.get(dateExampleList.size()-1);

        // converting from UTC to our ZoneTime the instant will be the same
        assertThat(dateExample.getTimestampWithZone()).isEqualTo("2019-12-27T11:39:34.326+00:00[Europe/Paris]");
        assertThat(dateExample.getTimestampWithUtcOffset()).isEqualTo("2019-12-27T11:39:34.326+01:00");

        // Reading Without Zone datetime is accidentally safe because it has been written in Paris
        assertThat(dateExample.getTimestampWithoutZone()).isEqualTo("2019-12-27T11:39:34.326");
    }

}
