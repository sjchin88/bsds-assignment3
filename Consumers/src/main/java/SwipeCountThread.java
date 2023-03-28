import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Like recording thread extends the Consumer Thread
 */
public class SwipeCountThread extends ConsumerThread{
  /**
   * Create a new thread to record the like and dislike
   * @param connection  RabbitMQ connection
   * @param exchangeName  Target ExchangeName
   * @param exchangeType  Target Exchange type, either "direct", "topic" or "fanout"
   * @param queueName   Name of target Queue
   * @param bindingKeys   binding keys
   * @param redisConn  an instance of Redis Command async() api
   * @throws IOException
   */
  public SwipeCountThread(Connection connection, String exchangeName,
      String exchangeType, String queueName, String[] bindingKeys,
      StatefulRedisConnection<String, String> redisConn
      )
      throws IOException {
    super(connection, exchangeName, exchangeType, queueName, bindingKeys, redisConn);
  }

  /**
   * Custom implementation of run method
   */
  @Override
  public void run() {

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      //ByteBuffer buffer = ByteBuffer.wrap(delivery.getBody());
      //int swiper = buffer.getInt();
      String[] messages = new String(delivery.getBody(), "UTF-8").split(":");

      //System.out.println(messages[0] + " " + messages[1]);
      //System.out.println("Swiper" + swiper);
      if(delivery.getEnvelope().getRoutingKey().equals("right")){
        String swiperId = "Likes:"+messages[0];
        RedisFuture<Long> future = this.redisCommand.incr(swiperId);
      } else {
        String swiperId = "DisLikes:"+messages[0];
        RedisFuture<Long> future = this.redisCommand.incr(swiperId);
      }
      // Debug code
      // System.out.println(" [x] Received from " + delivery.getEnvelope().getRoutingKey() + " swiperId:" + swiper);
    };
    try {
      this.channel.basicConsume(this.queueName, true, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      Logger.getLogger(LikeThread.class.getName()).log(Level.WARNING, "channel subscription fail", e);
    }
  }
}
