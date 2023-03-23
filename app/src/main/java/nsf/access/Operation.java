package nsf.access;

import java.util.Locale;

public enum Operation {

    PUT,
    READ,
    DELETE;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.US);
    }
}
