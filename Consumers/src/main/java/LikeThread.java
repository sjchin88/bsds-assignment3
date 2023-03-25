import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
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
  private ConcurrentHashMap<Integer, List<Integer>> swipeDB;

  /**
   * Create new swipe recording thread based on given arguments
   * @param connection  RabbitMQ connection
   * @param exchangeName  Target ExchangeName
   * @param exchangeType  Target Exchange type, either "direct", "topic" or "fanout"
   * @param queueName   Name of target Queue
   * @param bindingKeys   binding keys
   * @param swipeDB   ConcurrentHashMap used to record the swipee being liked by the swiper
   * @throws IOException
   */
  public LikeThread(Connection connection, String exchangeName,
      String exchangeType, String queueName, String[] bindingKeys, ConcurrentHashMap<Integer, List<Integer>> swipeDB) throws IOException {
    super(connection, exchangeName, exchangeType, queueName, bindingKeys);
    this.swipeDB = swipeDB;
  }

  /**
   * Custom implementation of run method
   */
  @Override
  public void run() {

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      ByteBuffer buffer = ByteBuffer.wrap(delivery.getBody());
      int swiper = buffer.getInt();
      int swipee = buffer.getInt();
      this.swipeDB.computeIfAbsent(swiper, k -> Collections.synchronizedList(new ArrayList<>(100))).add(swipee);
      // Debug code
      // System.out.println(" [x] Received from " + delivery.getEnvelope().getRoutingKey() + " swiperId:" + swiper + " swipeeId:" + swipee);
    };
    try {
      this.channel.basicConsume(this.queueName, true, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      Logger.getLogger(LikeThread.class.getName()).log(Level.WARNING, "channel subscription fail", e);
    }
  }
}
