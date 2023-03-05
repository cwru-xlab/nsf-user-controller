package nsf.pda.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

@Value.Immutable(copy = true)
@SuppressWarnings("DefaultAnnotationParam")
interface BaseAccessToken {

  @Value.Redacted
  @JsonProperty("accessToken")
  String value();

  String userId();
}
