package redis;

import io.vertx.core.buffer.Buffer;
import org.junit.Test;
import redis.reply.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test reading and writing replies.
 */
public class ReplyTest {
    @Test
    public void testReadWrite() throws IOException {
        ByteArrayOutputStream os;
        Reply receive;
        {
            os = new ByteArrayOutputStream();
            String message = "OK";
            new StatusReply(message).write(os);
            receive = RedisProtocol.receive(new ByteArrayInputStream(os.toByteArray()));
            assertTrue(receive instanceof StatusReply);
            assertEquals(message, receive.data());
        }
        {
            os = new ByteArrayOutputStream();
            String message = "OK";
            new ErrorReply(message).write(os);
            receive = RedisProtocol.receive(new ByteArrayInputStream(os.toByteArray()));
            assertTrue(receive instanceof ErrorReply);
            assertEquals(message, receive.data());
        }
        {
            os = new ByteArrayOutputStream();
            String message = "OK";
            new BulkReply(message.getBytes()).write(os);
            receive = RedisProtocol.receive(new ByteArrayInputStream(os.toByteArray()));
            System.out.println(((BulkReply) receive).asUTF8String());
            assertTrue(receive instanceof BulkReply);
            assertEquals(message, new String((byte[]) receive.data()));
        }
        {
            os = new ByteArrayOutputStream();
            long integer = 999;
            new IntegerReply(integer).write(os);
            receive = RedisProtocol.receive(new ByteArrayInputStream(os.toByteArray()));
            assertTrue(receive instanceof IntegerReply);
            assertEquals(integer, receive.data());
        }
        {
            os = new ByteArrayOutputStream();
            String message = "OK";
            long integer = 999;
            new MultiBulkReply(new Reply[]{
                    new StatusReply(message),
                    new ErrorReply(message),
                    new MultiBulkReply(new Reply[]{new StatusReply(message)}),
                    new BulkReply(message.getBytes()),
                    new IntegerReply(integer)}).write(os);
            receive = RedisProtocol.receive(new ByteArrayInputStream(os.toByteArray()));
            assertTrue(receive instanceof MultiBulkReply);
            Reply[] data = (Reply[]) receive.data();
            assertEquals(message, data[0].data());
            assertEquals(message, data[1].data());
            assertTrue(data[2] instanceof MultiBulkReply);
            Reply[] data2 = (Reply[]) data[2].data();
            assertEquals(message, data2[0].data());
            assertEquals(message, new String((byte[]) data[3].data()));
            assertEquals(integer, data[4].data());
        }
    }

    @Test
    public void test() {
        Buffer buffer = Buffer.buffer();

        Buffer buffer1 = Buffer.buffer();
        buffer1.appendString("Hellozxzczx");
        buffer1.getByteBuf().readByte();
        buffer.appendString("asda");
        buffer.appendBuffer(buffer1);

        System.out.println(buffer.getByteBuf().readerIndex());
        buffer.getByteBuf().readByte();
        buffer.getByteBuf().readByte();

        System.out.println(new String(buffer.getBuffer(0, buffer.getByteBuf().writerIndex()).getBytes()));
    }
}
