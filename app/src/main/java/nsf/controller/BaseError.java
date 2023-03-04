package nsf.controller;

import org.immutables.value.Value;

@Value.Immutable
abstract class BaseError {

  public abstract int code();

  public abstract String message();
}
