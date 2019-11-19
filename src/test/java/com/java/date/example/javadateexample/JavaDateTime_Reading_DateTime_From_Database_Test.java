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
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Rollback(false)
class JavaDateTime_Reading_DateTime_From_Database_Test {

    @Autowired
    private DateExampleRepository dateExampleRepository;

    @Test
    void when_Reading_DateTime_From_Database_In_London_Then_Expect_Correct_Values() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/London")));
        final List<DateExample> dateExampleList = dateExampleRepository.findAll();
        final DateExample dateExample = dateExampleList.get(0);

        // converting from UTC to our ZoneTime the instant will be the same
        assertThat(dateExample.getTimestampWithZone()).isEqualTo("2019-06-27T10:39:34.326+00:00[Europe/London]");
        assertThat(dateExample.getTimestampWithUtcOffset()).isEqualTo("2019-06-27T10:39:34.326+01:00");

        // Reading Without Zone datetime we can see the instant is not the correct one
        assertThat(dateExample.getTimestampWithoutZone()).isNotEqualTo("2019-06-27T10:39:34.326");
        assertThat(dateExample.getTimestampWithoutZone()).isEqualTo("2019-06-27T11:39:34.326");
    }

    @Test
    void when_Reading_DateTime_From_Database_In_Tokyo_Then_Expect_Correct_Values() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Tokyo")));
        final List<DateExample> dateExampleList = dateExampleRepository.findAll();
        final DateExample dateExample = dateExampleList.get(0);

        // converting from UTC to our ZoneTime the instant will be the same
        assertThat(dateExample.getTimestampWithZone()).isEqualTo("2019-06-27T18:39:34.326+00:00[Asia/Tokyo]");
        assertThat(dateExample.getTimestampWithUtcOffset()).isEqualTo("2019-06-27T18:39:34.326+09:00");

        // Reading Without Zone datetime we can see the instant is not the correct one
        assertThat(dateExample.getTimestampWithoutZone()).isNotEqualTo("2019-06-27T18:39:34.326");
        assertThat(dateExample.getTimestampWithoutZone()).isEqualTo("2019-06-27T11:39:34.326");
    }

    @Test
    void when_Reading_DateTime_From_Database_In_Paris_Then_Expect_Correct_Values() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/Paris")));
        final List<DateExample> dateExampleList = dateExampleRepository.findAll();
        final DateExample dateExample = dateExampleList.get(0);

        // converting from UTC to our ZoneTime the instant will be the same
        assertThat(dateExample.getTimestampWithZone()).isEqualTo("2019-06-27T11:39:34.326+00:00[Europe/Paris]");
        assertThat(dateExample.getTimestampWithUtcOffset()).isEqualTo("2019-06-27T11:39:34.326+02:00");

        // Reading Without Zone datetime is accidentally safe because it has been written in Paris
        assertThat(dateExample.getTimestampWithoutZone()).isEqualTo("2019-06-27T11:39:34.326");
    }
}
