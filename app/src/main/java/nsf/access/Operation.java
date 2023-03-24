package nsf.access;

import java.util.Locale;

public enum Operation {
    /**
     * Lets ServiceProviders automatically get pushed new data for the resources they are subscribed to.
     */
    SUBSCRIBE,
    READ,
    PUT,
    DELETE;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.US);
    }
}
