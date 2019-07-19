package io.ebeaninternal.server.type;

import io.ebean.text.TextException;
import io.ebeaninternal.server.core.BasicTypeConverter;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalTime;

/**
 * ScalarType for java.time.LocalTime stored as JDBC Time.
 */
public class ScalarTypeLocalTime extends ScalarTypeBase<LocalTime> {

  public ScalarTypeLocalTime() {
    super(LocalTime.class, false, Types.TIME);
  }

  protected ScalarTypeLocalTime(int jdbcTtype) {
    super(LocalTime.class, false, jdbcTtype);
  }

  @Override
  public void bind(DataBind bind, LocalTime value) throws SQLException {
    if (value == null) {
      bind.setNull(Types.TIME);
    } else {
      bind.setTime(Time.valueOf(value));
    }
  }

  @Override
  public LocalTime read(DataReader dataReader) throws SQLException {
    Time time = dataReader.getTime();
    return (time == null) ? null : time.toLocalTime();
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Time) return value;
    return Time.valueOf((LocalTime) value);
  }

  @Override
  public LocalTime toBeanType(Object value) {
    if (value instanceof LocalTime) return (LocalTime) value;
    if (value == null) return null;
    return BasicTypeConverter.toTime(value).toLocalTime();
  }

  @Override
  public LocalTime readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return LocalTime.ofNanoOfDay(dataInput.readLong());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, LocalTime value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeLong(value.toNanoOfDay());
    }
  }

  @Override
  public String formatValue(LocalTime v) {
    return v.toString();
  }

  @Override
  public LocalTime parse(String value) {
    return LocalTime.parse(value);
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public LocalTime convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not Supported");
  }

  @Override
  public LocalTime jsonRead(JsonParser parser) throws IOException {
    return LocalTime.parse(parser.getValueAsString());
  }

  @Override
  public void jsonWrite(JsonGenerator writer, LocalTime value) throws IOException {
    writer.writeString(value.toString());
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.KEYWORD;
  }

}
