package rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.io.IOException;

/**
 * rabbitmq.ConsumerThread for creating a thread connecting to RabbitMQ
 */
public class ConsumerThread implements Runnable{
  protected static final String PREFIX_LIKES_CNT = "Likes:";
  protected static final String PREFIX_DISLIKES_CNT = "Dislikes:";
  protected static final String PREFIX_SWIPE_REC = "Swiper:";
  protected Channel channel;
  protected String queueName;

  /**
   * Create new consumerThread based on given arguments
   * @param connection  RabbitMQ connection
   * @param exchangeName  Target ExchangeName
   * @param exchangeType  Target Exchange type, either "direct", "topic" or "fanout"
   * @param queueName   Name of target Queue
   * @param bindingKeys   binding keys
   */
  public ConsumerThread(Connection connection, String exchangeName, String exchangeType,
      String queueName, String[] bindingKeys) throws IOException {
    this.channel = setChannel(connection, exchangeName, exchangeType, queueName, bindingKeys);
    this.queueName = queueName;
  }

  /**
   * Class method to set up a new channel based on given arguments
   * @param connection  RabbitMQ connection
   * @param exchangeName  Target ExchangeName
   * @param exchangeType  Target Exchange type, either "direct", "topic" or "fanout"
   * @param queueName   Name of target Queue
   * @param bindingKeys   binding keys
   * @return channel set up
   * @throws IOException when creating the channel
   */
  public static Channel setChannel(Connection connection, String exchangeName, String exchangeType,
      String queueName, String[] bindingKeys) throws IOException {
    Channel channel = connection.createChannel();
    channel.exchangeDeclare(exchangeName, exchangeType, true);
    // queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String,Object> arguments)
    // If queue with queueName not yet exist, create a durable (second argument), non-exclusive (third argument)
    // non-autoDelete (fourth argument) queue
    channel.queueDeclare(queueName, true, false, false, null);
    // Create a binding between the queue and the exchange, using the binding keys
    for(String bindKey:bindingKeys){
      channel.queueBind(queueName, exchangeName, bindKey);
    }
    channel.basicQos(1);
    return channel;
  }

  /**
   * Custom implementation of run method
   */
  @Override
  public void run() {

  }
}
