/*
 * Copyright 2014-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neiljbrown.examples.java8;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

/**
 * Examples of the new, improved <a href="http://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html">Date
 * and Time API</a> available in Java 8, implemented as a JUnit test case.
 * 
 * <h2>Local and Time Zone Date/Times</h2>
 * <p>
 * The API has two separate categories of date/time classes - local and zoned.
 * <p>
 * Local date/time time classes ({@link LocalDateTime}, {@link LocalDate} and {@link LocalTime}) are “local” in the
 * sense that they don’t have any time zone context, and are akin to how people think of their (local) time.
 * <p>
 * A {@link ZonedDateTime} has a contextual time-zone. Use this class (rather than LocalDateTime) if you want to
 * represent a date and time without relying on the time-zone of a specific server.
 * 
 * <h2>Further Reading</h2>
 * <p>
 * <a href="http://www.oracle.com/technetwork/articles/java/jf14-date-time-2125367.html">JSE 8 Date and Time (overview),
 * Evans and Warburton, 02/2014</a> <br>
 * <a href="http://docs.oracle.com/javase/tutorial/datetime/index.html">Date and Time, Oracle Java Tutorial</a>
 */
public class DateTimeExamplesTest {

  /**
   * Date/time classes provide factory methods which follow naming conventions. of() is used to construct date/time from
   * their constituent field, from a year, up to a resolution of nanoseconds.
   */
  @Test
  public void testCreateDateTimeFromConstituentFields() {
    final LocalDateTime localDateTime = LocalDateTime.of(2014, Month.AUGUST, 30, 21, 40, 20);
    // toString() methods return ISO-8601 format representation by default, which is consistent with parsing (see below)
    assertThat(localDateTime.toString(), is("2014-08-30T21:40:20"));
  }

  /**
   * Date/time classes provide factory methods which follow naming conventions. parse() is used to construct date/time
   * from, by default an ISO-8601 format string.
   * <p>
   * Parse methods use the {@link java.time.format.DateTimeFormatter} which has constants for the supported date/time
   * string formats.
   */
  @Test
  public void testParseDateTime() {
    // Parse a local (no time zone) date/time string
    // Uses java.time.format.DateTimeFormatter#ISO_LOCAL_DATE_TIME.
    final LocalDateTime localdateTime = LocalDateTime.parse("2014-08-30T21:40:20");
    assertThat(localdateTime, is(LocalDateTime.of(2014, Month.AUGUST, 30, 21, 40, 20)));

    // Parse a date/time string with a time zone, in ISO-8601 format - this one is in UTC, as per the 'Z' zone indicator
    // Uses java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME.
    final ZonedDateTime zonedDateTime = ZonedDateTime.parse("2014-08-30T21:40:20Z");
    assertThat(zonedDateTime, is(ZonedDateTime.of(2014, Month.AUGUST.getValue(), 30, 21, 40, 20, 0, ZoneId.of("Z"))));
  }

  /**
   * Date/time classes provide getter methods for obtaining their component values (different component resolutions).
   */
  @Test
  public void testGetDateTimeComponentValues() {
    final Month month = Month.AUGUST;
    int dayOfMonth = 31;
    int seconds = 59;

    final LocalDateTime localDateTime = LocalDateTime.of(2014, month, dayOfMonth, 18, 46, seconds);

    assertThat(localDateTime.getMonth(), is(month));
    assertThat(localDateTime.getDayOfMonth(), is(dayOfMonth));
    assertThat(localDateTime.getSecond(), is(seconds));
  }

  /**
   * Date/time classes are immutable. Therefore methods to alter instances of them are named with() rather than being
   * setters, and they return new instances.
   */
  @Test
  public void testAlterDateTimeComponentValues() {
    final LocalDateTime localDateTime = LocalDateTime.of(2014, Month.AUGUST, 30, 19, 02, 58);

    // Test altering component fields with literal values using withXXX()
    int pastYear = 2013;
    int pastDayOfMonth = 1;
    final LocalDateTime pastDateTime = localDateTime.withYear(pastYear).withDayOfMonth(pastDayOfMonth);
    // Construct separate LocalDate and Time from supplied component values, and time component of existing DateTime
    LocalDate expectedDate = LocalDate.of(pastYear, localDateTime.getMonth(), pastDayOfMonth);
    final LocalTime expectedTime = localDateTime.toLocalTime();
    // A LocalDateTime can be constructed from separate LocalDate and LocalTime
    assertThat(pastDateTime, is(LocalDateTime.of(expectedDate, expectedTime)));

    // Test altering component fields using delta method plusXXX()
    LocalDateTime futureDateTime = localDateTime.plusWeeks(3);
    expectedDate = LocalDate.of(localDateTime.getYear(), Month.SEPTEMBER, 20);
    assertThat(futureDateTime, is(LocalDateTime.of(expectedDate, expectedTime)));
    // Test altering component fields using generic delta method plus(value, Unit)
    futureDateTime = futureDateTime.plus(1, ChronoUnit.WEEKS);
    expectedDate = LocalDate.of(localDateTime.getYear(), Month.SEPTEMBER, 27);
    assertThat(futureDateTime, is(LocalDateTime.of(expectedDate, expectedTime)));
  }

  /**
   * The API supports truncating a Date, Time and DateTime to different precisions. Truncation results in the fields
   * with precisions smaller than the specified unit being set to zero.
   */
  @Test
  public void testTruncationTo() {
    final LocalDateTime localDateTime = LocalDateTime.of(2014, Month.SEPTEMBER, 1, 15, 56, 52, 1);
    // Note - To set seconds and nanoseconds to zero, you truncate the minutes.
    LocalDateTime truncatedDateTime = localDateTime.truncatedTo(ChronoUnit.MINUTES);
    assertThat(truncatedDateTime, is(LocalDateTime.parse("2014-09-01T15:56:00")));
  }

  /**
   * Illustrates how a zoned date/time can be created using a {@link ZoneId} to specify the time zone region.
   * <p>
   * The following is a paraphrased from the javadoc for {@link java.time.ZoneId}. A ZoneId is an identifier for a
   * time-zone region.
   * <p>
   * Logically there are two main types of time zone IDs: <br>
   * 
   * 1) Fixed offset - A literal, hard-coded offset from UTC/GMT usually in terms of hours and/or mins. These zone IDs
   * are more typically represented using a {@link java.time.ZoneOffset}. <br>
   * 
   * 2) Region-based IDs - An identifier for a time zone based used for a particular geographic region. These IDs are
   * configured via a ZoneRulesProvider. The most commonly used representation of time zone region identifiers are
   * defined in the IANA Time Zone Database (TZDB) and are strings in the format "Area/Region", e.g. "Europe/London" or
   * "America/New_York".
   */
  @Test
  public void testZoneIdRegionAndFixedOffset() {
    // Create zoned date/time using ZoneId to parse an IANA area/region format TZ ID (use one that doesn't vary with
    // daylight saving to ensure this test-case is stable)
    final ZoneId zoneId = ZoneId.of("Africa/Addis_Ababa");
    final ZonedDateTime zonedDateTime = ZonedDateTime.of(2014, Month.AUGUST.getValue(), 30, 21, 40, 20, 0, zoneId);

    // Use ZoneOffset to return the fixed offset of a zoned date/time from UTC
    final ZoneOffset zoneOffset = zonedDateTime.getOffset();
    int threeHoursInSeconds = 3 * 60 * 60;
    assertThat(zoneOffset.getTotalSeconds(), is(threeHoursInSeconds));
  }

  /**
   * You can modify the values of dates (only) using {@link java.time.Period}.
   * <p>
   * A {@link java.time.Period} models a length of time in terms of years, months and days. It’s a ‘distance’ in a
   * timeline, as opposed to other classes (above) being a point on a timeline.
   */
  @Test
  public void testPeriodToAlterDate() {
    // 3 years, 2 months, 1 day
    final Period period = Period.of(3, 2, 1);
    // Components of a Period are represented by ChronoUnit values
    assertThat(period.get(ChronoUnit.DAYS), is(1L));

    // You can modify the values of a Date using a Period
    final LocalDate localDate = LocalDate.of(2014, Month.SEPTEMBER, 1);
    final LocalDate futureDate = localDate.plus(period);
    assertThat(futureDate, is(LocalDate.of(2017, Month.NOVEMBER, 2)));
    // You can also use a Period to calculate the difference between two dates
    assertThat(Period.between(localDate, futureDate), is(period));

    final LocalDate pastDate = localDate.minus(period);
    assertThat(pastDate, is(LocalDate.of(2011, Month.JUNE, 30)));
  }

  /**
   * You can modify the values of times (only) using {@link java.time.Duration}.
   * <p>
   * A {@link java.time.Duration} serves a similar purpose to {@link java.time.Period} except that it models a length of
   * time in terms of seconds and nanoseconds.
   */
  @Test
  public void testDurationToAlterTime() {
    // A duration of 3 seconds and 5 nanoseconds
    Duration duration = Duration.ofSeconds(3, 5);

    // You can modify the values of a DateTime using a Duration
    final LocalDateTime localDateTime = LocalDateTime.of(2014, Month.SEPTEMBER, 1, 17, 29, 40);
    final LocalDateTime futureDateTime = localDateTime.plus(duration);
    assertThat(futureDateTime, is(LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.of(17, 29, 43, 5))));
    // You can also use a Duration to calculate the difference between two times
    assertThat(Duration.between(localDateTime, futureDateTime), is(duration));
  }

  /**
   * {@link java.time.MonthDay} is a holder for an associated month and day, such as a birthday.
   */
  @Test
  public void testMonthDayHolder() {
    final MonthDay birthday = MonthDay.of(Month.SEPTEMBER, 1);
    final LocalDate localDate = LocalDate.of(2014, birthday.getMonthValue(), birthday.getDayOfMonth());

    assertThat(localDate.getMonth(), is(birthday.getMonth()));
  }

  /**
   * {@link java.time.YearMonth} is a holder for an associated year and month, such as that used for expiry dates.
   */
  @Test
  public void testYearMonthHolder() {
    final YearMonth expiry = YearMonth.of(2014, Month.SEPTEMBER);
    final LocalDate localDate = LocalDate.of(expiry.getYear(), expiry.getMonthValue(), 1);

    assertThat(localDate.getMonth(), is(expiry.getMonth()));
  }

  /**
   * Provides an example of using the new {@link java.time.Clock} class' {@link Clock#fixed(Instant, ZoneId)} method to
   * obtain a clock that always returns the same instant (current date/time).
   * <p>
   * The date/time API includes a new abstract class {@link java.time.Clock} which can be used to find the current
   * date/time (instant) for an identified time-zone. This duplicates a capability of the new date/time classes which
   * provide a now() factory method, and, for discovery of the current time in UTC, the traditional use of
   * {@link System#currentTimeMillis()}. However, Clock, is primarily intended to provide an injectable (non-static)
   * means of deriving the current time, to support and simplify testing of time sensitive code.
   * 
   * @throws Exception If an unexpected error occurs.
   */
  @Test
  public void testClockFixed() throws Exception {
    // A clock which always returns a fixed date/time - in this case 'now', in UTC
    Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));

    Instant instant1 = clock.instant();
    Thread.sleep(1);
    Instant instant2 = clock.instant();

    assertThat(instant2, is(instant1));
  }

  /**
   * Provides an example of using the new {@link java.time.Clock} class' {@link Clock#offset(Clock, Duration)} method to
   * obtain a clock that returns an instant which is always a specified duration before or after the current date/time.
   * <p>
   * For further explanation of the {@link java.time.Clock} class, see {@link #testClockFixed()}.
   * 
   * @throws Exception If an unexpected error occurs.
   */
  @Test
  public void testClockOffset() throws Exception {
    // A clock with a date/time which is always 1 hour behind the current system date/time, in UTC
    Clock clock = Clock.offset(Clock.systemUTC(), Duration.ofHours(-1));

    Thread.sleep(1);
    long currentTimeMillis = Instant.now().toEpochMilli();
    
    // The offset clock should return a date/time, behind the current date/time
    assertThat(clock.instant().toEpochMilli(), lessThan(currentTimeMillis));
  }
}