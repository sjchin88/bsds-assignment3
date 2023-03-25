import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Like recording thread extends the Consumer Thread
 */
public class SwipeThread extends ConsumerThread{
  private ConcurrentHashMap<Integer, Integer> likeDB;
  private ConcurrentHashMap<Integer, Integer> dislikeDB;

  /**
   * Create a new thread to record the like and dislike
   * @param connection  RabbitMQ connection
   * @param exchangeName  Target ExchangeName
   * @param exchangeType  Target Exchange type, either "direct", "topic" or "fanout"
   * @param queueName   Name of target Queue
   * @param bindingKeys   binding keys
   * @param likeDB    ConcurrentHashMap used to record the number of likes given by the swiper
   * @param dislikeDB ConcurrentHashMap used to record the number of dislikes given by the swiper
   * @throws IOException
   */
  public SwipeThread(Connection connection, String exchangeName,
      String exchangeType, String queueName, String[] bindingKeys,
      ConcurrentHashMap<Integer, Integer> likeDB, ConcurrentHashMap<Integer, Integer> dislikeDB)
      throws IOException {
    super(connection, exchangeName, exchangeType, queueName, bindingKeys);
    this.likeDB = likeDB;
    this.dislikeDB = dislikeDB;
  }

  /**
   * Custom implementation of run method
   */
  @Override
  public void run() {

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      ByteBuffer buffer = ByteBuffer.wrap(delivery.getBody());
      int swiper = buffer.getInt();
      if(delivery.getEnvelope().getRoutingKey().equals("right")){
        likeDB.compute(swiper,(key, val) -> (val == null) ? 1: val + 1);
      } else {
        dislikeDB.compute(swiper,(key, val) -> (val == null) ? 1: val + 1);
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
