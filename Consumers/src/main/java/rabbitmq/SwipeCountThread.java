package rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Like recording thread extends the Consumer Thread
 */
public class SwipeCountThread extends ConsumerThread{
  private BlockingQueue<String> buffer;


  /**
   * Create a new thread to record the like and dislike
   * @param connection  RabbitMQ connection
   * @param exchangeName  Target ExchangeName
   * @param exchangeType  Target Exchange type, either "direct", "topic" or "fanout"
   * @param queueName   Name of target Queue
   * @param bindingKeys   binding keys
   * @param
   * @throws IOException
   */
  public SwipeCountThread(Connection connection, String exchangeName,
      String exchangeType, String queueName, String[] bindingKeys,
      BlockingQueue<String> buffer
      )
      throws IOException {
    super(connection, exchangeName, exchangeType, queueName, bindingKeys);
    this.buffer = buffer;
  }

  /**
   * Custom implementation of run method
   */
  @Override
  public void run() {

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {

      String[] messages = new String(delivery.getBody(), "UTF-8").split(":");
      try {
        String swiperId="";
        if (delivery.getEnvelope().getRoutingKey().equals("right")) {
          swiperId = PREFIX_LIKES_CNT + messages[0];
        } else {
          swiperId = PREFIX_DISLIKES_CNT + messages[0];
        }
        this.buffer.put(swiperId);
      }
      catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

    };
    try {
      this.channel.basicConsume(this.queueName, true, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      Logger.getLogger(SwipeRecThread.class.getName()).log(Level.WARNING, "channel subscription fail", e);
    }
  }
}
