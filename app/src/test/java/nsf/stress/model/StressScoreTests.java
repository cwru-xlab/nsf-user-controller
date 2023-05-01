package nsf.stress.model;

import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class StressScoreTests {

  private StressScoreTests() {
  }

  @ParameterizedTest
  @MethodSource("validStressScoreValuesAndTimestamps")
  public void whenValidStressScoreArgumentsAreProvidedToBuilderThenNoExceptionIsThrown(
      double value, Instant timestamp) {
    Assertions.assertDoesNotThrow(() -> buildStressScore(value, timestamp));
  }

  @ParameterizedTest
  @MethodSource("invalidStressScoreValues")
  public void whenInvalidStressScoreValueIsProvidedToBuilderThenExceptionIsThrown(double value) {
    Assertions.assertThrowsExactly(IllegalStateException.class, () -> buildWithValue(value));
  }

  @Test
  public void whenStressScoreValueIsNotProvidedToBuilderThenExceptionIsThrown() {
    Assertions.assertThrowsExactly(
        IllegalStateException.class, StressScoreTests::buildWithMissingValue);
  }

  @Test
  public void whenStressScoreTimestampIsNotProvidedToBuilderThenExceptionIsThrown() {
    Assertions.assertThrowsExactly(
        IllegalStateException.class, StressScoreTests::buildWithMissingTimestamp);
  }

  private static Stream<Arguments> validStressScoreValuesAndTimestamps() {
    return Stream.of(
        Arguments.of(StressScore.MIN_VALUE, Instant.MIN),
        Arguments.of(StressScore.MIN_VALUE, Instant.MAX),
        Arguments.of(StressScore.MAX_VALUE, Instant.MIN),
        Arguments.of(StressScore.MAX_VALUE, Instant.MAX),
        Arguments.of(StressScore.MAX_VALUE / 4.56, Instant.ofEpochMilli(123456789))
    );
  }

  private static Stream<Arguments> invalidStressScoreValues() {
    return Stream.of(
            StressScore.MIN_VALUE - 0.123,
            Math.nextDown(StressScore.MIN_VALUE),
            Math.nextUp(StressScore.MAX_VALUE),
            StressScore.MAX_VALUE + 0.456)
        .map(Arguments::of);
  }

  private static void buildStressScore(double value, Instant timestamp) {
    StressScore.builder().value(value).timestamp(timestamp).build();
  }

  private static void buildWithValue(double value) {
    StressScore.builder().timestamp(Instant.MIN).value(value).build();
  }

  private static void buildWithMissingValue() {
    StressScore.builder().timestamp(Instant.MIN).build();
  }

  private static void buildWithMissingTimestamp() {
    StressScore.builder().value(StressScore.MIN_VALUE).build();
  }
}
