import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Swipe recording thread extends from Consumer Thread to
 * record the swiper and swipee into the hashmap
 */
public class LikeThread extends ConsumerThread{

  /**
   * Create new swipe recording thread based on given arguments
   * @param connection  RabbitMQ connection
   * @param exchangeName  Target ExchangeName
   * @param exchangeType  Target Exchange type, either "direct", "topic" or "fanout"
   * @param queueName   Name of target Queue
   * @param bindingKeys   binding keys
   * @param redisConn  an instance of Redis Command async() api
   * @throws IOException
   */
  public LikeThread(Connection connection, String exchangeName,
      String exchangeType, String queueName, String[] bindingKeys, StatefulRedisConnection<String, String> redisConn) throws IOException {
    super(connection, exchangeName, exchangeType, queueName, bindingKeys, redisConn);
  }

  /**
   * Custom implementation of run method
   */
  @Override
  public void run() {

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      ///ByteBuffer buffer = ByteBuffer.wrap(delivery.getBody());
      //int swiper = buffer.getInt();
      //int swipee = buffer.getInt();
      String[] messages = new String(delivery.getBody(), "UTF-8").split(":");
      String swiperId = "Swiper:"+messages[0];
      RedisFuture<Long> set = this.redisCommand.sadd(swiperId, messages[1]);
    };
    try {
      this.channel.basicConsume(this.queueName, true, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      Logger.getLogger(LikeThread.class.getName()).log(Level.WARNING, "channel subscription fail", e);
    }
  }
}
