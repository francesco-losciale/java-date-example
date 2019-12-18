package com.java.date.example.javadateexample;

import com.java.date.example.javadateexample.entities.DateExample;
import com.java.date.example.javadateexample.repositories.DateExampleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
@Rollback(false)
class JavaTimePackageTest {

    @Autowired
    private DateExampleRepository dateExampleRepository;

    // Time distance between Paris and UTC/GMT/London is 1 hour.
    // Be careful when you see values saved in a table WITH ZONE
    // During the Daylight Saving Time (31 March to 27 October in UK)
    // The UTC offset from Paris is +02:00 hours instead of only one

    final ZonedDateTime parisZoned = ZonedDateTime.parse("2019-12-27T11:00:00.000+01:00[Europe/Paris]");
    final ZonedDateTime londonZoned = ZonedDateTime.parse("2019-12-27T10:00:00.000+00:00[Europe/London]");

    private static DateExample dateExample = null;

    @BeforeEach
    void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/London")));
        if (dateExample == null) {
            dateExample = new DateExample();
            dateExample.setTimestampWithoutZone(parisZoned.toLocalDateTime());
            dateExample.setTimestampWithZone(parisZoned);
            dateExample.setTimestampWithUtcOffset(parisZoned.toOffsetDateTime());
            dateExampleRepository.saveAndFlush(dateExample);
            // The timestamp with time zone is a zone-aware date and time data type.
            // PostgreSQL stores the timestamp in UTC value.
            // When you insert a value into the column, PostgreSQL converts the timestamp
            // into a UTC value and stores the UTC value in the table.
            // When you query from the database, PostgreSQL converts the UTC value back
            // to the time value of the timezone set by the database server, the user, or the current
            // database connection.
        }
    }

    @AfterEach
    void tearDown() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/London")));
    }

    @Test
    void when_EuropeParisZoned_Is_Compared_EuropeLondonZoned_Then_Equals() {
        assertThat(parisZoned).isEqualTo(londonZoned);
    }

    @Test
    void when_EuropeParisZoned_Is_Converted_to_EuropeLondonZoned_Then_Correct_Value() {
        ZonedDateTime converted = parisZoned.withZoneSameInstant(ZoneId.of("Europe/London"));
        assertThat(converted).isEqualTo(londonZoned);
        assertThat(converted.getOffset().getTotalSeconds()).isEqualTo(0);
    }

    @Test
    void when_Converting_EuropeParis_To_Utc_Then_Offset_is_One_Hour() {
        assertThat(parisZoned.toOffsetDateTime()).isEqualTo(OffsetDateTime.parse("2019-12-27T11:00:00.000+01:00"));
        assertThat(parisZoned.toOffsetDateTime().getOffset()).isEqualTo(ZoneOffset.ofHours(1));
    }

    @Test
    void when_Converting_Zoned_EuropeParis_To_Local_EuropeParis_Then_Offset_And_Zone_Ignored() {
        assertThat(parisZoned.toLocalDateTime()).isEqualTo(LocalDateTime.parse("2019-12-27T11:00:00.000"));
    }

    @Test
    void when_Converting_Utc_EuropeParis_To_Local_EuropeParis_Then_Offset_Ignored() {
        assertThat(parisZoned.toOffsetDateTime().toLocalDateTime()).isEqualTo(LocalDateTime.parse("2019-12-27T11:00:00.000"));
    }

    @Test
    void when_Converting_Local_EuropeParis_To_UTC_Then_Value_Is_Correct() {
        LocalDateTime londonDateTime = LocalDateTime.parse("2019-12-27T11:00:00.000");
        OffsetDateTime utcDateTime = ZonedDateTime.of(londonDateTime, ZoneId.of("Europe/Paris"))
                .toOffsetDateTime();
        assertThat(utcDateTime).isEqualTo(parisZoned.toOffsetDateTime());
    }

    @Test
    void when_Converting_Utc_To_EuropeLondon_Then_Value_Is_Correct() {
        OffsetDateTime utcDateTime = OffsetDateTime.parse("2019-12-27T10:00:00.000Z");
        LocalDateTime parisDateTime = utcDateTime
                .atZoneSameInstant(ZoneId.of("Europe/Paris"))
                .toLocalDateTime();
        assertThat(parisDateTime).isEqualTo(LocalDateTime.parse("2019-12-27T11:00:00.000"));
    }

    @Test
    void when_Doing_Date_Calculation_Using_Utc_Dates_Then_Result_Is_Not_Different_From_Doing_Them_With_Local_DateTime() {
        OffsetDateTime utcDateTime = OffsetDateTime.parse("2019-12-27T10:00:00.000Z");
        LocalDateTime localDateTime = convertToLondonLocalDateTime(utcDateTime);

        utcDateTime = utcDateTime.plusDays(1).plusHours(2).plusMinutes(6);
        localDateTime = localDateTime.plusDays(1).plusHours(2).plusMinutes(6);

        assertThat(utcDateTime).isEqualTo(convertToUtcDateTime(localDateTime));
        assertThat(localDateTime).isEqualTo(convertToLondonLocalDateTime(utcDateTime));
    }

    private OffsetDateTime convertToUtcDateTime(LocalDateTime localDateTime) {
        return ZonedDateTime.of(localDateTime, ZoneId.of("Europe/London"))
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);
    }

    private LocalDateTime convertToLondonLocalDateTime(OffsetDateTime utcDateTime) {
        return utcDateTime.atZoneSameInstant(ZoneId.of("Europe/London"))
                                                .toLocalDateTime();
    }

    @Test
    void when_Reading_DateTime_From_Database_In_London_Then_Expect_Correct_Values() {
        final List<DateExample> dateExampleList = dateExampleRepository.findAll();
        final DateExample dateExample = dateExampleList.get(dateExampleList.size() - 1);

        // converting from UTC to our ZoneTime the instant will be the same
        assertThat(dateExample.getTimestampWithZone()).isEqualTo("2019-12-27T10:00:00.000+00:00[Europe/London]");
        assertThat(dateExample.getTimestampWithZone()).isEqualTo("2019-12-27T11:00:00.000+01:00[Europe/Paris]");
        assertThat(dateExample.getTimestampWithUtcOffset()).isEqualTo("2019-12-27T11:00:00.000+01:00");

        // What we saved as LocalDateTime (in a column WITHOUT TIME ZONE) has ignored the offset/zone
        // The instant in our local Europe/London Timezone is wrong and one hour ahead.
        assertThat(dateExample.getTimestampWithoutZone()).isNotEqualTo("2019-12-27T10:00:00.000");
        assertThat(dateExample.getTimestampWithoutZone()).isEqualTo("2019-12-27T11:00:00.000");
    }

    @Test
    void when_Reading_DateTime_From_Database_In_Tokyo_Then_Expect_Correct_Values() {

        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Tokyo")));

        final List<DateExample> dateExampleList = dateExampleRepository.findAll();
        final DateExample dateExample = dateExampleList.get(dateExampleList.size() - 1);

        // converting from the UTC value in the table to our ZoneTime the instant will be the same
        assertThat(dateExample.getTimestampWithZone()).isEqualTo("2019-12-27T19:00:00.000+08:00[Asia/Tokyo]");
        assertThat(dateExample.getTimestampWithUtcOffset()).isEqualTo("2019-12-27T19:00:00.000+09:00");

        // Reading Without Zone datetime we can see the instant is not the correct one
        assertThat(dateExample.getTimestampWithoutZone()).isNotEqualTo("2019-12-27T18:00:00.000");
        assertThat(dateExample.getTimestampWithoutZone()).isEqualTo("2019-12-27T11:00:00.000");
    }

    @Test
    void when_Reading_DateTime_From_Database_In_Paris_Then_Expect_Correct_Values() {

        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/Paris")));

        final List<DateExample> dateExampleList = dateExampleRepository.findAll();
        final DateExample dateExample = dateExampleList.get(dateExampleList.size() - 1);

        // converting from the UTC value in the table to our ZoneTime the instant will be the same
        assertThat(dateExample.getTimestampWithZone()).isEqualTo("2019-12-27T11:00:00.000+01:00[Europe/Paris]");
        assertThat(dateExample.getTimestampWithUtcOffset()).isEqualTo("2019-12-27T11:00:00.000+01:00");

        // Reading Without Zone datetime is accidentally safe because it has been written in the Paris zone
        assertThat(dateExample.getTimestampWithoutZone()).isEqualTo("2019-12-27T11:00:00.000");
    }

}
