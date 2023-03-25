package server;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A RabbitMQ Channel pool based on BlockingQueue implementation
 * Credit: Ian Gorton, https://github.com/gortonator/foundations-of-scalable-systems/tree/main/Ch7
 */
public class RabbitMQChannelPool {
  private final BlockingQueue<Channel> pool;
  private int capacity;
  private RabbitMQChannelFactory factory;

  /**
   * Construct new RabbitMQ Channel Pool based on given parameter
   * @param capacity capacity of the pool
   * @param factory RabbitMQChannelFactory used
   */
  public RabbitMQChannelPool(int capacity,
      RabbitMQChannelFactory factory) {
    this.pool = new LinkedBlockingDeque<>(capacity);
    this.capacity = capacity;
    this.factory = factory;
    for(int i = 0; i < capacity; i++){
      Channel channel;
      try{
        channel = factory.create();
        pool.put(channel);
      } catch (IOException | InterruptedException e) {
        Logger.getLogger(RabbitMQChannelPool.class.getName()).log(Level.SEVERE, null, e);
      }
    }
  }

  /**
   * Method to get the channel from the pool for use
   * @return Channel connect to RabbitMQ
   */
  public Channel borrowObject() {
    try {
      return this.pool.take();
    } catch (InterruptedException e) {
      throw new RuntimeException("Error: no channels available" + e.toString());
    }
  }

  /**
   * Return the channel given back into the pool
   * @param channel Channel connect to RabbitMQ
   */
  public void returnObject(Channel channel){
    if (channel != null){
      pool.add(channel);
    }
  }
}
