import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import rabbitmq.SwipeCountThread;
import redis.RedisSwipeCountThread;
import redis.RedisSwipeRecThread;

/**
 * Driver to record the likes and dislikes count from the swiper
 */
public class SwipeCountRecorder extends Recorder{
  /**
   * Default queue name
   */
  protected static final String QUEUE_NAME = "swipesrecord";
  private static final String[] BINDING_KEYS = new String[] {"right", "left"};

  /**
   * Main method, take in the number of thread as first argument and server address as second argument
   * if there is problem parsing any of this the default will be used
   * @param argv CLI arguments
   * @throws Exception
   */
  public static void main(String[] argv) throws Exception {
    int numThread;
    try{
      numThread = Integer.valueOf(argv[0]);
    } catch (Exception e){
      numThread = NUM_THREAD;
    }
    String serverAddr;
    try{
      serverAddr = argv[1];
    } catch (Exception e){
      serverAddr = SERVER_ADDR;
    }
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(serverAddr);
    //factory.setUsername(ADMIN_NAME);
    //factory.setPassword(ADMIN_PASS);
    Connection connection = factory.newConnection();

    BlockingQueue<String> buffer = new LinkedBlockingDeque<>(500_000);
    for(int i = 0; i < numThread; i++){
      Thread thread = new Thread(new SwipeCountThread(connection, EXCHANGE_NAME, EXCHANGE_TYPE, QUEUE_NAME, BINDING_KEYS, buffer));
      thread.start();
    }
    for(int i = 0; i < 3; i++){
      Thread redisThread = new Thread(new RedisSwipeCountThread(buffer));
      redisThread.start();
    }
  }
}
