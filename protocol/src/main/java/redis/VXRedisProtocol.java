package redis;

import io.netty.buffer.ByteBuf;
import redis.reply.BulkReply;
import redis.reply.IntegerReply;
import redis.reply.MultiBulkReply;
import redis.reply.Reply;

import java.io.EOFException;
import java.io.IOException;

/**
 * Created by Nguyen Dang Chien on 7/27/16.
 */
public class VXRedisProtocol {
    public static final char CR = '\r';
    public static final char LF = '\n';
    private static final char ZERO = '0';

    private ByteBuf byteBuf;

    public VXRedisProtocol(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    /**
     * Read fixed size field from the stream.
     *
     * @return
     * @throws IOException
     */
    public byte[] readBytes() throws IOException {
        long size = readLong();
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Java only supports arrays up to " + Integer.MAX_VALUE + " in size");
        }
        int read;
        if (size == -1) {
            return null;
        }
        if (size < 0) {
            throw new IllegalArgumentException("Invalid size: " + size);
        }

        byte[] bytes = new byte[(int) size];
        int length = bytes.length;
        int total = getUnreadByte();
        if (total < length) {
            throw new IOException("Failed to read enough bytes: " + total);
        }

        byteBuf.readBytes(
                bytes,
                0,
                length
        );

        int cr = byteBuf.readByte();
        int lf = byteBuf.readByte();
        if (cr != CR || lf != LF) {
            throw new IOException("Improper line ending: " + cr + ", " + lf);
        }
        return bytes;
    }

    /**
     * Read a signed ascii integer from the input stream.
     *
     * @return
     * @throws IOException
     */
    public long readLong() throws IOException {
        int sign;
        int read = byteBuf.readByte();
        if (read == '-') {
            read = byteBuf.readByte();
            sign = -1;
        } else {
            sign = 1;
        }

        long number = 0;
        do {
            if (read == -1) {
                throw new EOFException("Unexpected end of stream");
            } else if (read == CR) {
                if (byteBuf.readByte() == LF) {
                    return number * sign;
                }
            }
            int value = read - ZERO;
            if (value >= 0 && value < 10) {
                number *= 10;
                number += value;
            } else {
                throw new IOException("Invalid character in integer");
            }
            read = byteBuf.readByte();
        } while (true);
    }

    /**
     * Read a Reply from an input stream.
     *
     * @return
     * @throws IOException
     */
    public Reply receive() throws IOException {
        int code = byteBuf.readByte();
        if (code == -1) {
            throw new EOFException();
        }
        switch (code) {
//            case StatusReply.MARKER: {
//                return new StatusReply(new DataInputStream(is).readLine());
//            }
//            case ErrorReply.MARKER: {
//                return new ErrorReply(new DataInputStream(is).readLine());
//            }
            case IntegerReply.MARKER: {
                return new IntegerReply(readLong());
            }
            case BulkReply.MARKER: {
                return new BulkReply(readBytes());
            }
            case MultiBulkReply.MARKER: {
                return new MultiBulkReply(getMultiBulkReply());
            }
            default: {
                throw new IOException("Unexpected character in stream: " + code);
            }
        }
    }

    public Reply[] getMultiBulkReply() throws IOException {
        long size = readLong();
        if (size == -1) {
            return null;
        } else {
            if (size > Integer.MAX_VALUE || size < 0) {
                throw new IllegalArgumentException("Invalid size: " + size);
            }
            Reply[] replies = new Reply[(int) size];
            for (int i = 0; i < size; i++) {
                replies[i] = receive();
            }
            return replies;
        }
    }

    public byte[] toBytes(Number length) {
        return length.toString().getBytes();
    }

    public int getUnreadByte() {
        return byteBuf.writerIndex() - byteBuf.readerIndex();
    }
}
