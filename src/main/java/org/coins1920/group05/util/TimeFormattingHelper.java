package org.coins1920.group05.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import static java.time.temporal.ChronoField.*;

public class TimeFormattingHelper {

    private static final String DEFAULT_ZONE_ID = "Europe/Berlin";

    /**
     * Computes a Condor-compatible (e.g. "2019-11-10T21:17:36.000+01:00") timestamp
     * from a GitHub timestamp.
     *
     * @param githubTimestamp the GitHub timestamp
     * @return the Condor-compatible timestamp
     */
    public static String githubTimestampToCondorTimestamp(String githubTimestamp) {
        // GitHub timestamps are ISO 8601-formatted UTC timestamps:
        final Instant instant = Instant.parse(githubTimestamp);

        final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendValue(YEAR, 4)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 2)
                .appendLiteral('-')
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral('T')
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .appendFraction(NANO_OF_SECOND, 3, 3, true)
                .appendOffset("+HH:MM", "+00:00")
                .toFormatter(Locale.GERMANY)
                .withZone(ZoneId.of(DEFAULT_ZONE_ID));

        return formatter.format(instant);
    }

    /**
     * Return NOW as an ISO 8601-formatted, UTC-based timestamp, just like
     * GitHub, e.g. "2019-12-07T14:11:51Z".
     *
     * @return ISO 8601 now
     */
    public static String now() {
        final Instant now = Instant
                .now()
                .truncatedTo(ChronoUnit.SECONDS);

        return DateTimeFormatter
                .ISO_INSTANT
                .withZone(ZoneId.of(DEFAULT_ZONE_ID))
                .format(now);
    }

    /**
     * Parses an ISO 8601-formatted timestamp (e.g. "2019-12-07T14:11:51Z")
     * and returns an Instant.
     *
     * @param timestamp the ISO 8601-formatted timestamp
     * @return the corresponding Instant
     */
    public static Instant parseIso8601Timestamp(String timestamp) {
        return LocalDateTime
                .parse(timestamp, DateTimeFormatter.ISO_DATE_TIME)
                .atZone(ZoneId.of(DEFAULT_ZONE_ID))
                .toInstant();
    }

    public static String unixEpoch() {
        return "1970-01-01T00:00:00Z";
    }
}
