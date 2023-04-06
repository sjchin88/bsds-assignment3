package services.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * RabbitMQ channel factory based on the apache pool libraries
 * Credit: Ian Gorton, https://github.com/gortonator/foundations-of-scalable-systems/tree/main/Ch7
 */
public class RabbitMQChannelFactory extends BasePooledObjectFactory<Channel> {

  /**
   * Default exchange_name used by the channels
   */
  private static final String EXCHANGE_NAME = "swipes";
  /**
   * Address of the RabbitMQ server, change it to IP address when hosting on EC-2
   */
  private static String RABBIT_HOST = "localhost";
  //private static String RABBIT_HOST = "35.165.32.0";
  //private static String RABBIT_USER = "csj";
  //private static String RABBIT_PASS = "Gu33ssm3";
  private ConnectionFactory rabbitFactory;
  // Connection for RMQ
  private Connection rmqConnection;
  private int channelCount;


  /**
   * Create new RabbitMQ channel factory using given RabbitMQ connection
   * @param rmqConnection a valid connection to RabbitMQ
   */
  public RabbitMQChannelFactory(Connection rmqConnection) {
    this.rmqConnection = rmqConnection;
    this.channelCount = 0;
  }

  /**
   * Create new RabbitMQ channel factory based on default connection setting
   */
  public RabbitMQChannelFactory() {
    // Create new connection to the rabbit MQ
    this.rabbitFactory = new ConnectionFactory();
    //this.rabbitFactory.setHost(RABBIT_HOST);
    //this.rabbitFactory.setUsername(RABBIT_USER);
    //this.rabbitFactory.setPassword(RABBIT_PASS);
    Connection rabbitMQConn;
    try {
      rabbitMQConn = this.rabbitFactory.newConnection();
      System.out.println("INFO: RabbitMQ connection established");
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
    this.rmqConnection = rabbitMQConn;
    this.channelCount = 0;
  }

  /**
   * Create the RabbitMQ channel threadsafe way
   * @return channel created
   * @throws Exception
   */
  @Override
  synchronized public Channel create() throws IOException {
    this.channelCount++;
    Channel channel = this.rmqConnection.createChannel();
    // third argument is always the durable boolean, set it true so exchange will survive node restart
    channel.exchangeDeclare(EXCHANGE_NAME,"direct", true);
    return channel;
  }

  /**
   * Wrap the channel into a PooledObject and return
   * @param channel given RabbitMQ channel
   * @return PooledObject <Channel>
   */
  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<>(channel);
  }

  /**
   * Getter for channelCount, used for debugging
   * @return channelCount
   */
  public int getChannelCount() {
    return this.channelCount;
  }
}
