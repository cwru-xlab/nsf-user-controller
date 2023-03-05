package nsf.pda;

import org.immutables.value.Value;

@Value.Immutable
interface BaseAccessToken {

  String accessToken();

  String userId();
}
