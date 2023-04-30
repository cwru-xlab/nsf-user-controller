package nsf.stress.model;

import java.util.stream.Stream;
import nsf.stress.HealthRecords;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class HealthRecordTests {

  private HealthRecordTests() {
  }

  @Test
  public void whenAllValidValuesAreProvidedToBuilderThenNoExceptionIsThrown() {
    Assertions.assertDoesNotThrow(HealthRecords.validBuilder()::build);
  }

  @ParameterizedTest
  @MethodSource("buildersWithMissingAttributes")
  public void whenValuesAreNotProvidedToBuilderThenExceptionIsThrown(HealthRecord.Builder builder) {
    Assertions.assertThrowsExactly(IllegalStateException.class, builder::build);
  }

  @ParameterizedTest
  @MethodSource("buildersWithInvalidAttributes")
  public void whenInvalidValuesAreProvidedToBuilderThenExceptionIsThrown(
      HealthRecord.Builder builder) {
    Assertions.assertThrowsExactly(IllegalStateException.class, builder::build);
  }

  private static Stream<Arguments> buildersWithMissingAttributes() {
    return Stream.of(
            HealthRecords.builderWithMissingActiveDuration(),
            HealthRecords.builderWithMissingAverageSpeed(),
            HealthRecords.builderWithMissingExpenditure(),
            HealthRecords.builderWithMissingHeartRates(),
            HealthRecords.builderWithMissingSleepDuration(),
            HealthRecords.builderWithMissingStepCount())
        .map(Arguments::of);
  }

  private static Stream<Arguments> buildersWithInvalidAttributes() {
    return Stream.of(
            HealthRecords.builderWithInfiniteAverageSpeed(),
            HealthRecords.builderWithInfiniteExpenditure(),
            HealthRecords.builderWithNaNAverageSpeed(),
            HealthRecords.builderWithNaNAverageSpeed(),
            HealthRecords.builderWithNegativeActiveDuration(),
            HealthRecords.builderWithNegativeAverageSpeed(),
            HealthRecords.builderWithNegativeExpenditure(),
            HealthRecords.builderWithNegativeHeartRates(),
            HealthRecords.builderWithNegativeSleepDuration(),
            HealthRecords.builderWithNegativeStepCount())
        .map(Arguments::of);
  }
}
