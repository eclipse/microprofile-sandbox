package org.eclipse.microprofile.streams.example.jsonb;

import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;
import java.util.UUID;

public class JsonbUuidDeserializer implements JsonbDeserializer<UUID> {
  @Override
  public UUID deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
    JsonValue value = parser.getValue();
    if (value.getValueType() == JsonValue.ValueType.NULL) {
      return null;
    } else if (value.getValueType() != JsonValue.ValueType.STRING) {
      throw new JsonbException("UUID must be a STRING, but was " + value.getValueType());
    } else {
      return UUID.fromString(((JsonString) value).getString());
    }
  }
}
