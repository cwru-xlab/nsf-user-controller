package nsf.pda.data;

import java.util.Locale;

public enum Ordering {

  ASCENDING,
  DESCENDING;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.US);
  }
}
