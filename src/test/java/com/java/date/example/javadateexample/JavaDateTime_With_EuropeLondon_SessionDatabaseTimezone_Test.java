package com.java.date.example.javadateexample;

import com.java.date.example.javadateexample.entities.DateExample;
import com.java.date.example.javadateexample.repositories.DateExampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.jdbc.time_zone = Europe/London"})
@Rollback(false)
class JavaDateTime_With_EuropeLondon_SessionDatabaseTimezone_Test {

    @Autowired
    private DateExampleRepository dateExampleRepository;

    @BeforeEach
    void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/London")));
    }

    @Test
    void should_Jvm_TimeZone_Be_Equal_To_Database_TimeZone() {
        assertThat(
                TimeZone.getDefault()
        ).isEqualTo(
                TimeZone.getTimeZone(ZoneId.of("Europe/London")) // the db timezone
        );
        // if they are not, then hours will not be aligned in the table column.
        // the database will try to convert to its own timezone, so for example:
        // if the db timezone is Europe/London,
        // and you persist a Europe/Paris LocalDateTime of "2019-06-27 12:39:34.326000",
        // then you will find in your db a TIMESTAMP WITHOUT ZONE as
        // 2019-06-27 13:39:34.326000
    }

    @Test
    void when_received_Zoned_datetime_with_over_network_then_Verify_proper_conversions() {
        final ZonedDateTime zonedDateTime = ZonedDateTime.parse("2019-06-27T11:39:34.326+01:00[Europe/Paris]");
        final LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        final OffsetDateTime offsetDateTime = zonedDateTime.toOffsetDateTime();

        assertThat(zonedDateTime).isEqualTo(
                ZonedDateTime.parse("2019-06-27T11:39:34.326+01:00[Europe/Paris]")
        );
        // ZoneId Europe/Paris = (UTC+02:00) - OffsetDateTime is an UTC date with offset
        assertThat(offsetDateTime).isEqualTo(
                OffsetDateTime.parse("2019-06-27T12:39:34.326+02:00")
        );
        // LocalDate taken in the Europe/Paris ZoneId
        assertThat(localDateTime).isEqualTo(
                LocalDateTime.parse("2019-06-27T12:39:34.326")
        );
        DateExample dateExample = new DateExample();
        dateExample.setTimestampWithoutZone(localDateTime);
        dateExample.setTimestampWithZone(zonedDateTime);
        dateExample.setTimestampWithUtcOffset(offsetDateTime);
        dateExampleRepository.saveAndFlush(dateExample);

        final DateExample readDateExample = dateExampleRepository.getOne(dateExample.getId());
        assertThat(zonedDateTime).isEqualTo(
                readDateExample.getTimestampWithZone()
        );
        assertThat(localDateTime).isEqualTo(
                readDateExample.getTimestampWithoutZone()
        );
        assertThat(offsetDateTime).isEqualTo(
                readDateExample.getTimestampWithUtcOffset()
        );
    }
}
