package net.ostrekalovsky.rat.service.storage;

import lombok.Data;

@Data
public class DBState {

    private final String origin;
    private int offset;
    private boolean processed;

    DBState(String origin, int offset, boolean processed) {
        this.origin = origin;
        this.offset = offset;
        this.processed = processed;
    }

    void moveForward() {
        offset++;
    }

    void setProcessed(){
        processed = true;
    }
}
