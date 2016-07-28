package redis;

import io.vertx.core.buffer.Buffer;

/**
 * Created by Nguyen Dang Chien on 7/27/16.
 */
public class CacheBufStorage {
    private Buffer buffer;

    public CacheBufStorage(Buffer buffer) {
        this.buffer = buffer;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }
}
