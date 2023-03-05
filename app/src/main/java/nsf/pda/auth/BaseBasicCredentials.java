package nsf.pda.auth;

import org.immutables.value.Value;

@Value.Immutable
interface BaseBasicCredentials extends Credentials {

  @Value.Parameter(order = 1)
  String username();

  @Value.Redacted
  @Value.Parameter(order = 2)
  String password();
}
