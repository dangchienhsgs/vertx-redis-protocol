package redis.reply;

import com.google.common.base.Charsets;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import redis.RedisProtocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: sam
 * Date: 7/29/11
 * Time: 10:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class BulkReply implements Reply<byte[]> {
    public static final char MARKER = '$';
    private final byte[] bytes;

    public BulkReply(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public byte[] data() {
        return bytes;
    }

    public String asAsciiString() {
        if (bytes == null) return null;
        return new String(bytes, Charsets.US_ASCII);
    }

    public String asUTF8String() {
        if (bytes == null) return null;
        return new String(bytes, Charsets.UTF_8);
    }

    public String asString(Charset charset) {
        if (bytes == null) return null;
        return new String(bytes, charset);
    }

    @Override
    public void write(OutputStream os) throws IOException {
        os.write(MARKER);
        os.write(RedisProtocol.toBytes(bytes.length));
        os.write(CRLF);
        os.write(bytes);
        os.write(CRLF);
    }

    @Override
    public void write(NetSocket socket) throws IOException {
        socket.write(Buffer.buffer().appendInt(MARKER));
        socket.write(Buffer.buffer().appendBytes(RedisProtocol.toBytes(bytes.length)));
        socket.write(Buffer.buffer().appendBytes(CRLF));
        socket.write(Buffer.buffer().appendBytes(bytes));
        socket.write(Buffer.buffer().appendBytes(CRLF));
    }
}
