package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.*;

public class ScalarTypeZonedDateTimeTest {


  ScalarTypeZonedDateTime type = new ScalarTypeZonedDateTime(JsonConfig.DateTime.MILLIS);

  ZonedDateTime warmUp = ZonedDateTime.now();

  @Test
  public void testConvertToMillis() throws Exception {

    warmUp.hashCode();

    long now = System.currentTimeMillis();
    long toMillis = type.convertToMillis(ZonedDateTime.now());

    assertTrue(toMillis - now < 10);

  }

  @Test
  public void testConvertFromTimestamp() throws Exception {

    Timestamp now = new Timestamp(System.currentTimeMillis());

    ZonedDateTime val1 = type.convertFromTimestamp(now);
    Timestamp timestamp = type.convertToTimestamp(val1);

    assertEquals(now, timestamp);
  }


  @Test
  public void testToJdbcType() throws Exception {

    Object jdbcType = type.toJdbcType(ZonedDateTime.now());
    assertTrue(jdbcType instanceof Timestamp);
  }

  @Test
  public void testToBeanType() throws Exception {

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    ZonedDateTime localDateTime = type.toBeanType(timestamp);

    assertNotNull(localDateTime);

    Timestamp timestamp1 = type.convertToTimestamp(localDateTime);
    assertEquals(timestamp, timestamp1);

  }

  @Test
  public void testJson() throws Exception {

    ZonedDateTime now = ZonedDateTime.now().withNano(123_000_000); // jdk11 workaround

    JsonTester<ZonedDateTime> jsonTester = new JsonTester<>(type);
    jsonTester.test(now);

    ScalarTypeZonedDateTime typeNanos = new ScalarTypeZonedDateTime(JsonConfig.DateTime.NANOS);
    jsonTester = new JsonTester<>(typeNanos);
    jsonTester.test(now);

    ScalarTypeZonedDateTime typeIso = new ScalarTypeZonedDateTime(JsonConfig.DateTime.ISO8601);
    jsonTester = new JsonTester<>(typeIso);
    jsonTester.test(now);
  }

  @Test
  public void toJsonISO8601() {

    ScalarTypeZonedDateTime typeIso = new ScalarTypeZonedDateTime(JsonConfig.DateTime.ISO8601);

    ZonedDateTime now = ZonedDateTime.now();
    String asJson = typeIso.toJsonISO8601(now);

    ZonedDateTime value = typeIso.fromJsonISO8601(asJson);
    assertThat(now).isEqualTo(value);
  }
}
