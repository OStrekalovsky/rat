package net.ostrekalovsky.rat.service.storage;

import lombok.Data;
import net.jcip.annotations.NotThreadSafe;

/**
 * Idempotency key for insertion. See {@link RenewableStorageService} for usage details.
 */
@Data
@NotThreadSafe
public class DBState {

    /**
     * Origin of the data.
     */
    private final String origin;
    /**
     * Last successful uploaded offset of the data.
     */
    private int offset;
    /**
     * Was origin of the data fully processed and stored.
     */
    private boolean processed;

    DBState(String origin, int offset, boolean processed) {
        this.origin = origin;
        this.offset = offset;
        this.processed = processed;
    }

    void moveForward() {
        offset++;
    }

    void setProcessed() {
        processed = true;
    }
}
