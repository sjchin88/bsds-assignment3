package rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Envelope;
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
      BlockingQueue<String> buffer, int preFetchCount
      )
      throws IOException {
    super(connection, exchangeName, exchangeType, queueName, bindingKeys, preFetchCount);
    this.buffer = buffer;
  }

  /**
   * Custom implementation of run method
   */
  @Override
  public void run() {

    Consumer batchConsumer = new DefaultConsumer(channel) {
      Envelope lastMessageEnvelope;

      private Envelope getLastMessageEnvelope() {
        return lastMessageEnvelope;
      }

      private void setLastMessageEnvelope(Envelope lastMessageEnvelope) {
        this.lastMessageEnvelope = lastMessageEnvelope;
      }
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
          byte[] body) throws IOException {
        String[] messages = new String(body, "UTF-8").split(":");
        try {
          String swiperId="";
          if (envelope.getRoutingKey().equals("right")) {
            swiperId = PREFIX_LIKES_CNT + messages[0];
          } else {
            swiperId = PREFIX_DISLIKES_CNT + messages[0];
          }
          buffer.put(swiperId);
        }
        catch (InterruptedException e) {
          System.out.println("Rabbit interrupt exception");
        }
        setLastMessageEnvelope(envelope);
      }

      @Override
      public void handleCancelOk(String consumerTag) {
        try {
          if(lastMessageEnvelope != null) {
            channel.basicAck(getLastMessageEnvelope().getDeliveryTag(), true);
          } else {
            System.out.println("No messages are avaiable in queue : " + queueName);
          }
        } catch (IOException e) {
          System.out.println("Error in sending ACK " + e);
        }
      }
    };

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
        System.out.println("Rabbit interrupt exception");
      }

    };
    try {
      this.channel.basicConsume(this.queueName, false, batchConsumer);
    } catch (IOException e) {
      Logger.getLogger(SwipeRecThread.class.getName()).log(Level.WARNING, "channel subscription fail", e);
      System.out.println("channel subscription fail");
    }
  }
}
