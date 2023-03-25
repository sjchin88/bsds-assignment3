import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;

/**
 * ConsumerThread for creating a thread connecting to RabbitMQ
 */
public class ConsumerThread implements Runnable{
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
    channel.exchangeDeclare(exchangeName, exchangeType);
    // queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String,Object> arguments)
    // If queue with queueName not yet exist, create a non-durable (second argument), non-exclusive (third argument)
    // non-autoDelete (fourth argument) queue
    channel.queueDeclare(queueName, false, false, false, null);
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
