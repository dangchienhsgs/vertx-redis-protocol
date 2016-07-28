package redis;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import redis.exception.CommandErrorException;
import redis.exception.IncompleteCommandException;
import redis.exception.InvalidDigitCharacterException;
import redis.reply.BulkReply;
import redis.reply.IntegerReply;
import redis.reply.MultiBulkReply;
import redis.reply.Reply;

import java.io.IOException;

/**
 * Created by Nguyen Dang Chien on 7/27/16.
 */
public class VXRedisProtocol {
    public static final char CR = '\r';
    public static final char LF = '\n';
    private static final char ZERO = '0';

    private Buffer buffer;
    private ByteBuf byteBuf;
    private CacheBufStorage cacheBufStorage;

    private int unreadIndex;

    public VXRedisProtocol(Buffer buffer, CacheBufStorage cacheBufStorage) {
        this.byteBuf = buffer.getByteBuf();
        this.buffer = buffer;
        this.cacheBufStorage = cacheBufStorage;
    }

    public static byte[] toBytes(Number length) {
        return length.toString().getBytes();
    }

    public Reply receive(boolean inMultiBulkReply) throws CommandErrorException {
        if (!inMultiBulkReply) {
            // start new command
            unreadIndex = byteBuf.readerIndex();
            //System.out.println("Start new command at " + unreadIndex);
        }

        int code = byteBuf.readByte();

        try {

            if (code == IntegerReply.MARKER) {
                return new IntegerReply(readLong());
            } else if (code == BulkReply.MARKER) {
                return new BulkReply(readBytes());
            } else if (code == MultiBulkReply.MARKER) {
                return new MultiBulkReply(getMultiBulkReply());
            } else {
                // command error
                System.out.println("ERROR " + getNumberUnreadByte() + " " + ((char) code) + " " + new String(buffer.getBytes(unreadIndex, byteBuf.writerIndex())).replace("\r", "r").replace("\n", "n") + " " + buffer.toString().replace("\r", "r").replace("\n", "n") + " " + unreadIndex);
                throw new CommandErrorException("Unexpected character in stream: " + ((char) code));
            }
        } catch (IncompleteCommandException e) {
            if (!inMultiBulkReply) {
                cacheIncompleteCommand();
            }
            return null;
        }
    }

    public Reply[] getMultiBulkReply() throws IncompleteCommandException, CommandErrorException {
        long size = readLong();
        if (size == -1) {
            return null;
        } else {
            if (size > Integer.MAX_VALUE || size < 0) {
                throw new CommandErrorException("Invalid size: " + size);
            }
            Reply[] replies = new Reply[(int) size];
            for (int i = 0; i < size; i++) {
                if (getNumberUnreadByte() == 0) {
                    throw new IncompleteCommandException("The array command is incomplement at element " + i);
                }

                replies[i] = receive(true);

                if (replies[i] == null) {
                    throw new IncompleteCommandException("The array command is incomplement at element " + i);
                }
            }
            return replies;
        }
    }

    /**
     * Read a signed ascii integer from the input stream.
     * Must be handle when do not have enough
     *
     * @return
     * @throws IOException
     */
    public long readLong() throws IncompleteCommandException, InvalidDigitCharacterException {
        if (getNumberUnreadByte() < 3) {
            // need at least 3 byte to present ....\r\n
            throw new IncompleteCommandException("Do not have at least 3 byte to read a long");
        }

        // read the first byte
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
            // at least 1 digit was read
            if (read == CR) {
                if (getNumberUnreadByte() < 1) {
                    throw new IncompleteCommandException("Has '\\r' but not 'rn'");
                }
                if (byteBuf.readByte() == LF) {
                    return number * sign;
                }
            }

            int value = read - ZERO;
            if (value >= 0 && value < 10) {
                number *= 10;
                number += value;
            } else {
                throw new InvalidDigitCharacterException("The value " + value + " is not a digit");
            }

            if (getNumberUnreadByte() < 1) {
                throw new IncompleteCommandException("Get long do not have end symbol");
            }

            read = byteBuf.readByte();
        } while (true);
    }

    /**
     * Read fixed size field from the stream.
     *
     * @return
     * @throws IOException
     */
    public byte[] readBytes() throws CommandErrorException, IncompleteCommandException {
        long size;
        try {
            size = readLong();
        } catch (InvalidDigitCharacterException e) {
            // command error
            e.printStackTrace();
            throw new CommandErrorException("The command is error");
        }

        if (size > Integer.MAX_VALUE) {
            throw new CommandErrorException("Java only supports arrays up to " + Integer.MAX_VALUE + " in size");
        }
        if (size == -1) {
            return null;
        }
        if (size < 0) {
            throw new CommandErrorException("Invalid size: " + size);
        }

        byte[] bytes = new byte[(int) size];
        int length = bytes.length;

        if (getNumberUnreadByte() < length + 2) {
            // not enough bytes to read with that length
            throw new IncompleteCommandException("Failed to read enough bytes: " + getNumberUnreadByte() + "< " + length + 2);
        }

        byteBuf = byteBuf.readBytes(bytes, 0, length);
        int cr = byteBuf.readByte();
        int lf = byteBuf.readByte();
        if (cr != CR || lf != LF) {
            // not contains end symbol --> the command is not complete
            throw new IncompleteCommandException("Improper line ending: " + ((char) cr) + ", " + ((char) lf));
        }
        return bytes;
    }

    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    public int getNumberUnreadByte() {
        return byteBuf.writerIndex() - byteBuf.readerIndex();
    }

    public void cacheIncompleteCommand() {
        Buffer cacheBuffer = buffer.getBuffer(unreadIndex, buffer.getByteBuf().writerIndex());
        cacheBufStorage.setBuffer(cacheBuffer);
    }

    public int getReaderIndex() {
        return byteBuf.readerIndex();
    }

    public int getWriterIndex() {
        return byteBuf.writerIndex();
    }

    public boolean updateUnreadIndex(int newIndex) {
        if (newIndex >= getWriterIndex()) {
            return false;
        }

        unreadIndex = newIndex;
        return true;
    }
}
