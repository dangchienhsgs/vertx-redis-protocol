import com.google.common.base.Stopwatch;
import io.vertx.core.buffer.Buffer;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.reply.BulkReply;
import redis.reply.MultiBulkReply;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nguyen Dang Chien on 7/28/16.
 */
public class TestJedisCompability {

    @Test
    public void testJedis() throws Exception {
        Jedis jedis = new Jedis("127.0.0.1", 1234);

        Stopwatch stopwatch = Stopwatch.createUnstarted();

        stopwatch.start();
        for (int i = 0; i < 100000; i++) {
            List<String> list = jedis.hmget("mykey", "myvalue");
            System.out.println(list);
        }
        stopwatch.stop();

        System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    @Test
    public void testMultiBulk() {
        BulkReply reply1 = new BulkReply("abc".getBytes());
        BulkReply reply2 = new BulkReply("xyz".getBytes());

        MultiBulkReply multiBulkReply = new MultiBulkReply(new BulkReply[]{reply1, reply2});

        Buffer buffer = Buffer.buffer();

        buffer.appendByte((byte) '*');
        System.out.println(buffer);
    }
}
