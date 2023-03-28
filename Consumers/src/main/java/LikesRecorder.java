import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Driver to record the swipee liked by the swiper
 */
public class LikesRecorder extends Recorder{
  /**
   * Default queue name
   */
  protected static final String QUEUE_NAME = "likesrecord";
  /**
   * Default binding keys
   */
  private static final String[] BINDING_KEYS = new String[] {"right"};

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
    Connection rabbitConnection = factory.newConnection();

    //Set up Redis connection
    RedisClient redisClient = RedisClient.create(REDIS_HOST);
    StatefulRedisConnection<String, String> redisConnection = redisClient.connect();
    //Create asynchronous API
    //RedisAsyncCommands<String, String> redisAsyncCommands = redisConnection.async();
    for(int i = 0; i < numThread; i++){
      Thread thread = new Thread(new LikeThread(rabbitConnection, EXCHANGE_NAME, EXCHANGE_TYPE, QUEUE_NAME, BINDING_KEYS, redisConnection));
      thread.start();
    }
  }
}
