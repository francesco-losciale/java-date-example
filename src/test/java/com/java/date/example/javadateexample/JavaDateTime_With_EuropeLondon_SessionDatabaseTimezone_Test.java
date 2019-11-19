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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.jdbc.time_zone = Europe/London"})
class JavaDateTime_With_EuropeLondon_SessionDatabaseTimezone_Test {

    @Autowired
    private DateExampleRepository dateExampleRepository;

    final ZonedDateTime parisZoned = ZonedDateTime.parse("2019-06-27T11:39:34.326+00:00[Europe/Paris]");
    final OffsetDateTime parisUtc = parisZoned.toOffsetDateTime();
    final LocalDateTime parisLocal = parisZoned.toLocalDateTime();
    final ZonedDateTime londonZonedSameParisInstant = parisZoned.withZoneSameInstant(ZoneId.of("Europe/London"));

    @Test
    void when_Converting_EuropeParis_To_Utc_Then_Offset_is_Two_Hours() {
        assertThat(parisUtc).isEqualTo(OffsetDateTime.parse("2019-06-27T11:39:34.326+02:00"));
        assertThat(parisUtc.getOffset()).isEqualTo(ZoneOffset.ofHours(2));
    }

    @Test
    void when_Converting_EuropeParis_To_Local_EuropeLondon_Then_Offset_Is_Ignored() {
        assertThat(parisLocal).isEqualTo(LocalDateTime.parse("2019-06-27T11:39:34.326"));
    }

    @Test
    void when_Converting_EuropeParis_To_Zoned_EuropeLondon_Then_Offset_Is_One_Hour() {
        assertThat(londonZonedSameParisInstant.toOffsetDateTime()).isEqualTo(OffsetDateTime.parse("2019-06-27T10:39:34.326+01:00"));
        assertThat(londonZonedSameParisInstant.toOffsetDateTime().getOffset()).isEqualTo(ZoneOffset.ofHours(1));
    }

    @Test
    @Rollback(false)
    void when_Saving_Always_Use_UTC_Format_So_That_Who_Reads_Your_Date_Can_Convert_It_To_Their_Local_Timezone() {
        DateExample dateExample = new DateExample();
        dateExample.setTimestampWithoutZone(parisLocal);
        dateExample.setTimestampWithZone(parisZoned);
        dateExample.setTimestampWithUtcOffset(parisUtc);
        dateExampleRepository.saveAndFlush(dateExample);

        final DateExample readDateExample = dateExampleRepository.getOne(dateExample.getId());
        assertThat(readDateExample.getTimestampWithUtcOffset()).isEqualTo(
                dateExample.getTimestampWithUtcOffset()
        );
        assertThat(readDateExample.getTimestampWithZone()).isEqualTo(
                dateExample.getTimestampWithZone()
        );
        assertThat(readDateExample.getTimestampWithoutZone()).isEqualTo(
                dateExample.getTimestampWithoutZone()
        );
    }

}
