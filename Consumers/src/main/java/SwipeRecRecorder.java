import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import rabbitmq.SwipeRecThread;
import redis.RedisSwipeRecThread;

/**
 * Driver to record the swipee liked by the swiper
 */
public class SwipeRecRecorder extends Recorder{
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
    int numReddisThread;
    try{
      numReddisThread = Integer.valueOf(argv[1]);
    } catch (Exception e){
      numReddisThread = NUM_REDDIS_THREAD;
    }
    String serverAddr;
    try{
      serverAddr = argv[2];
    } catch (Exception e){
      serverAddr = RABBIT_HOST;
    }

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(serverAddr);
    //factory.setUsername(ADMIN_NAME);
    //factory.setPassword(ADMIN_PASS);
    Connection rabbitConnection = factory.newConnection();
    BlockingQueue<String[]> buffer = new LinkedBlockingDeque<>(BUFFER_SIZE);
    for(int i = 0; i < numThread; i++){
      Thread thread = new Thread(new SwipeRecThread(rabbitConnection, EXCHANGE_NAME, EXCHANGE_TYPE, QUEUE_NAME, BINDING_KEYS, buffer));
      thread.start();
    }
    for(int i = 0; i < numReddisThread; i++){
      Thread redisThread = new Thread(new RedisSwipeRecThread(buffer));
      redisThread.start();
    }
  }
}
