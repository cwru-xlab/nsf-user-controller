package nsf.stress;

import io.vertx.core.json.JsonObject;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class HealthRecordParserTests {

  private HealthRecordParser parser;

  private HealthRecordParserTests() {
  }

  @BeforeEach
  public void setUp() {
    parser = new HealthRecordParser();
  }

  @Test
  public void whenParsingCompleteJsonThenNoExceptionIsThrown() {
    Assertions.assertDoesNotThrow(() -> parser.parse(HealthRecords.validJson()));
  }

  @ParameterizedTest
  @MethodSource("jsonWithMissingData")
  public void whenParsingJsonWithMissingDataThenExceptionIsThrown(JsonObject json) {
    Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> parser.parse(json));
  }

  @ParameterizedTest
  @MethodSource("jsonWithInvalidTypes")
  public void whenParsingJsonWithInvalidTypesThenExceptionIsThrown(JsonObject json) {
    Assertions.assertThrowsExactly(ClassCastException.class, () -> parser.parse(json));
  }

  @Test
  public void whenParsingJsonWithInvalidHeatRateTimeEncodingThenExceptionIsThrown() {
    Assertions.assertThrowsExactly(
        DateTimeParseException.class,
        () -> parser.parse(HealthRecords.jsonWithInvalidHeartRatesTimeEncoding()));
  }

  private static Stream<Arguments> jsonWithMissingData() {
    return Stream.of(
            HealthRecords.jsonWithMissingActiveDuration(),
            HealthRecords.jsonWithMissingAverageSpeed(),
            HealthRecords.jsonWithMissingExpenditure(),
            HealthRecords.jsonWithMissingHeartRates(),
            HealthRecords.jsonWithMissingSleepDuration(),
            HealthRecords.jsonWithMissingStepCount())
        .map(Arguments::of);
  }

  private static Stream<Arguments> jsonWithInvalidTypes() {
    return Stream.of(
            HealthRecords.jsonWithInvalidActiveDurationType(),
            HealthRecords.jsonWithInvalidAverageSpeedType(),
            HealthRecords.jsonWithInvalidExpenditureType(),
            HealthRecords.jsonWithInvalidHeartRatesType(),
            HealthRecords.jsonWithInvalidHeartRatesTimeType(),
            HealthRecords.jsonWithInvalidHeartRatesValueType(),
            HealthRecords.jsonWithInvalidSleepDurationType(),
            HealthRecords.jsonWithInvalidStepCountType())
        .map(Arguments::of);
  }
}
